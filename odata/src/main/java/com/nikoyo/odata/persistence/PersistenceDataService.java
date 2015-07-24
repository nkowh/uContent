package com.nikoyo.odata.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikoyo.odata.RequestContext;
import com.nikoyo.odata.UriInfoUtils;
import com.nikoyo.odata.file.FileSystem;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.AbstractEdmAnnotatable;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.uri.*;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.queryoption.ExpandOptionImpl;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.WriteConsistencyLevel;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.deletebyquery.IndexDeleteByQueryResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.io.stream.InputStreamStreamInput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;

@Service
public class PersistenceDataService {

    private static Field edmField;

    static {
        edmField = FieldUtils.getField(AbstractEdmAnnotatable.class, "edm", true);
    }

    private EdmEntityType seekType(String namespaceAndName, EdmType seed) throws ODataApplicationException {
        try {
            Edm edm = (Edm) edmField.get(seed);
            return edm.getEntityType(new FullQualifiedName(namespaceAndName));
        } catch (IllegalAccessException e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault(), e);
        }
    }

    @Autowired
    RequestContext reqctx;

    @Autowired
    private Client client;

    @Autowired
    private FileSystem fs;


    public JsonCollection readEntityCollection(UriInfo uriInfo, OData odata, ServiceMetadata serviceMetadata) throws ODataApplicationException {
        UriInfoContext ctx = getUriInfoContext(uriInfo);

        if (ctx.edmNavigationProperty != null) {
            JsonCollection jsonObjs = retrieveRefEntityCollection(ctx, uriInfo.asUriInfoResource());
            return jsonObjs;
        } else {
            JsonCollection jsonObjs = retrieveEntityCollection(ctx, uriInfo.asUriInfoResource());
            return jsonObjs;
        }
    }

    public int countEntityCollection(UriInfo uriInfo) throws ODataApplicationException {
        return 0;
    }

    public JsonObj readEntity(UriInfo uriInfo, OData odata, ServiceMetadata serviceMetadata) throws ODataApplicationException {
        UriInfoContext ctx = getUriInfoContext(uriInfo);
        return ctx.entity;
    }

    public JsonObj createEntity(UriInfo uriInfo, ODataRequest request, OData odata, ServiceMetadata serviceMetadata) throws ODataApplicationException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return createEntity(uriInfo, mapper.readValue(new AutoCloseInputStream(request.getBody()), Map.class), odata, serviceMetadata);
        } catch (Exception e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault());
        }
    }

    public JsonObj createEntity(UriInfo uriInfo, Map<String, Object> source, OData odata, ServiceMetadata serviceMetadata) throws ODataApplicationException {
        try {
            UriInfoContext ctx = getUriInfoContext(uriInfo);
            EdmNavigationProperty edmNavigationProperty = ctx.edmNavigationProperty;
            ObjectMapper mapper = new ObjectMapper();
            ODataValidation.entity(odata, ctx.getType(), mapper.writeValueAsBytes(source));
            JsonObj entity = JsonObj.parse(source, ctx.getType());

            IndexResponse indexResponse = client.prepareIndex(reqctx.getRepositoryId(), entity.getType().getFullQualifiedName().getFullQualifiedNameAsString(), entity.getKeyObj().toBase64())
                    .setSource(source).setOpType(IndexRequest.OpType.INDEX).setRefresh(false).execute().actionGet();

            entity.setId(indexResponse.getId());

            if (ctx.entity != null) {
                createRef(edmNavigationProperty.getName(), ctx.entity.getKeyObj(), entity.getKeyObj());
            }
            return entity;

        } catch (Exception e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault());
        }
    }


    private void createRef(String navigationName, JsonObj source, JsonObj target) throws ODataApplicationException {
        JsonObj ref = JsonObj.simple();
        ref.put("source", source.getId());
        ref.put("sourceType", source.getType().getFullQualifiedName().getFullQualifiedNameAsString());
        ref.put("target", target.getId());
        ref.put("targetType", target.getType().getFullQualifiedName().getFullQualifiedNameAsString());
        String type = String.format("%s@%s", source.getType().getFullQualifiedName().getFullQualifiedNameAsString(), navigationName);
        IndexResponse indexResponse = client.prepareIndex(reqctx.getRepositoryId(), type)
                .setSource(ref)
                .execute().actionGet();
    }

    private JsonObj retrieveEntity(JsonObj condition, ExpandOption expandOption) throws ODataApplicationException {

        GetResponse getResponse = client.prepareGet(reqctx.getRepositoryId(), condition.getType().getFullQualifiedName().getFullQualifiedNameAsString(), condition.getKeyObj().toBase64())
                .execute().actionGet();
        if (!getResponse.isExists()) {
            throw new ODataApplicationException("Entity Not Found", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }

        EdmEntityType entityType = seekType(getResponse.getType(), condition.getType());
        JsonObj entityObj = JsonObj.parse(getResponse.getSourceAsString(), entityType);
        entityObj.setId(getResponse.getId());

        return retrieveEntity(expandOption, entityObj);
    }

    private JsonObj retrieveEntity(ExpandOption expandOption, JsonObj entityObj) throws ODataApplicationException {

        for (ExpandItem expandItem : expandOption.getExpandItems()) {
            UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) expandItem.getResourcePath().getUriResourceParts().get(0);
            EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
            UriInfoContext ctx = new UriInfoContext();
            ctx.entity = entityObj;
            ctx.edmNavigationProperty = edmNavigationProperty;
            List<JsonObj> jsonObjList = retrieveRefEntityCollection(ctx, expandItem.getResourcePath());
            if (edmNavigationProperty.isCollection()) {
                entityObj.put(edmNavigationProperty.getName(), jsonObjList);
            } else {
                entityObj.put(edmNavigationProperty.getName(), jsonObjList.get(0));
            }
        }

        return entityObj;
    }

    private JsonCollection retrieveEntityCollection(UriInfoContext ctx, UriInfoResource uriInfoResource) throws ODataApplicationException {
        return retrieveEntityCollection(ctx.getType(), uriInfoResource, null);
    }

    private JsonCollection retrieveRefEntityCollection(UriInfoContext ctx, UriInfoResource uriInfoResource) throws ODataApplicationException {
        int top = uriInfoResource.getTopOption() == null ? 10 : uriInfoResource.getTopOption().getValue();
        int skip = uriInfoResource.getSkipOption() == null ? 0 : uriInfoResource.getSkipOption().getValue();
        String orderBy = UriInfoUtils.getOrderBy(uriInfoResource.getOrderByOption());
        ExpandOption expandOption = uriInfoResource.getExpandOption() == null ? new ExpandOptionImpl() : uriInfoResource.getExpandOption();
        String filter = filterVisitor(uriInfoResource.getFilterOption());
        String type = String.format("%s@%s", ctx.entity.getType().getFullQualifiedName().getFullQualifiedNameAsString(), ctx.edmNavigationProperty.getName());

        SearchResponse searchResponse = client.prepareSearch(reqctx.getRepositoryId())
                .setTypes(type)
                .setSize(100)
                .setQuery(QueryBuilders.matchQuery("source", ctx.entity.getId()))
                .execute().actionGet();

        List<String> targets = new ArrayList<>();
        List<String> types = new ArrayList<>();
        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
            targets.add(searchHit.getSource().get("target").toString());
            types.add(searchHit.getSource().get("targetType").toString());
        }

        searchResponse = client.prepareSearch(reqctx.getRepositoryId())
                .setTypes(types.toArray(new String[]{}))
                .setQuery(QueryBuilders.idsQuery().addIds(types.toArray(new String[]{})).addIds(targets.toArray(new String[]{})))
                .execute().actionGet();


        JsonCollection entityCollection = new JsonCollection();
        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
            EdmEntityType entityType = seekType(searchHit.getType(), ctx.getType());
            JsonObj entityObj = JsonObj.parse(searchHit.getSourceAsString(), entityType);
            entityObj.setId(searchHit.getId());
            entityCollection.add(retrieveEntity(expandOption, entityObj));
        }
        entityCollection.setType(ctx.getType());
        return entityCollection;
    }


    private JsonCollection retrieveEntityCollection(EdmEntityType edmEntityType, UriInfoResource uriInfoResource, String baseSQL) throws ODataApplicationException {
        int top = uriInfoResource.getTopOption() == null ? 10 : uriInfoResource.getTopOption().getValue();
        int skip = uriInfoResource.getSkipOption() == null ? 0 : uriInfoResource.getSkipOption().getValue();
        String orderBy = UriInfoUtils.getOrderBy(uriInfoResource.getOrderByOption());
        ExpandOption expandOption = uriInfoResource.getExpandOption() == null ? new ExpandOptionImpl() : uriInfoResource.getExpandOption();
        String filter = filterVisitor(uriInfoResource.getFilterOption());
        SearchResponse searchResponse = client.prepareSearch(reqctx.getRepositoryId())
                .setTypes(edmEntityType.getFullQualifiedName().getFullQualifiedNameAsString())
                .setSize(top).setFrom(skip).execute().actionGet();

        JsonCollection entityCollection = new JsonCollection();
        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
            EdmEntityType entityType = seekType(searchHit.getType(), edmEntityType);
            JsonObj entityObj = JsonObj.parse(searchHit.getSourceAsString(), entityType);
            entityObj.setId(searchHit.getId());
            entityCollection.add(retrieveEntity(expandOption, entityObj));
        }
        entityCollection.setType(edmEntityType);
        return entityCollection;

    }

    private String filterVisitor(FilterOption filterOption) throws ODataApplicationException {
        if (filterOption == null) return " 1=1 ";
        FilterVisitor visitor = new FilterVisitor();
        return (String) visitor.visit(filterOption.getExpression());

    }

    public void updateEntity(UriInfo uriInfo, ODataRequest request, OData odata, ServiceMetadata serviceMetadata) throws ODataApplicationException {
        try {
            UriInfoContext ctx = getUriInfoContext(uriInfo);
            byte[] content = IOUtils.toByteArray(new AutoCloseInputStream(request.getBody()));
            JsonObj updateEntity = JsonObj.parse(content, ctx.getType());
            for (String propName : ctx.entity.getType().getPropertyNames()) {
                if (ctx.entity.isKey(propName)) continue;
                if (request.getMethod().equals(HttpMethod.PUT)) ctx.entity.remove(propName);
                if (updateEntity.containsKey(propName)) {
                    ctx.entity.put(propName, updateEntity.get(propName));
                }
            }

            updateEntity(ctx.entity);
        } catch (IOException e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault());
        }
    }

    private void updateEntity(JsonObj entity) throws ODataApplicationException {
        UpdateResponse updateResponse = client.prepareUpdate(reqctx.getRepositoryId(), entity.getType().getFullQualifiedName().getFullQualifiedNameAsString(), entity.getId())
                .setDoc(entity).execute().actionGet();

        if (StringUtils.isBlank(updateResponse.getId()))
            throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.getDefault());
    }

    public void deleteEntity(UriInfo uriInfo, OData odata, ServiceMetadata serviceMetadata) throws ODataApplicationException {
        UriInfoContext ctx = getUriInfoContext(uriInfo);
        DeleteResponse deleteResponse = client.prepareDelete(reqctx.getRepositoryId(), ctx.entity.getType().getFullQualifiedName().getFullQualifiedNameAsString(), ctx.entity.getId())
                .execute().actionGet();
        if (!deleteResponse.isFound())
            throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.getDefault());
        List<String> types = new ArrayList<>();
        for (String navigationnName : ctx.entity.getType().getNavigationPropertyNames()) {
            types.add(String.format("%s@%s", ctx.entity.getType().getFullQualifiedName().getFullQualifiedNameAsString(), navigationnName));
        }

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.minimumNumberShouldMatch(1);
        boolQueryBuilder.should(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("sourceType", ctx.entity.getType().getFullQualifiedName().getFullQualifiedNameAsString()))
                .must(QueryBuilders.matchQuery("source", ctx.entity.getId())));
        boolQueryBuilder.should(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("targetType", ctx.entity.getType().getFullQualifiedName().getFullQualifiedNameAsString()))
                .must(QueryBuilders.matchQuery("target", ctx.entity.getId())));
        DeleteByQueryResponse deleteByQueryResponse = client.prepareDeleteByQuery(reqctx.getRepositoryId()).setTypes(types.toArray(new String[]{})).setQuery(boolQueryBuilder)
                .execute().actionGet();
        deleteByQueryResponse.iterator().forEachRemaining(response -> {
            if (response.getFailedShards() > 0)
                try {
                    throw new ODataApplicationException("delete failed", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault());
                } catch (ODataApplicationException e) {
                    e.printStackTrace();
                }
        });

    }

    public void createReference(UriInfo uriInfo, ODataRequest request, OData odata, ServiceMetadata serviceMetadata) throws ODataApplicationException {
        try {
            UriInfoContext ctx = getUriInfoContext(uriInfo);
            ODataDeserializer deserializer = odata.createDeserializer(ODataFormat.JSON);
            DeserializerResult result = deserializer.entityReferences(request.getBody());

            int rawBaseUriIndex = request.getRawBaseUri().length();
            for (URI uri : result.getEntityReferences()) {
                UriInfo refUriInfo = new Parser().parseUri(uri.toString().substring(rawBaseUriIndex), null, null, serviceMetadata.getEdm());
                UriInfoContext refContext = getUriInfoContext(refUriInfo);
                JsonObj keyParams = JsonObj.parse(refContext.keyParams, ctx.edmNavigationProperty.getType());
                GetResponse getResponse = client.prepareGet(reqctx.getRepositoryId(), ctx.edmNavigationProperty.getType().getFullQualifiedName().getFullQualifiedNameAsString(), keyParams.toBase64())
                        .execute().actionGet();
                if (!getResponse.isExists())
                    throw new ODataApplicationException(String.format("Object:'%s' not found", uri.toString()), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
                keyParams.setId(getResponse.getId());
                createRef(ctx.edmNavigationProperty.getName(), ctx.entity.getKeyObj(), keyParams);
            }

        } catch (DeserializerException e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        } catch (Exception e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault());
        } finally {

        }
    }

    public void deleteReference(UriInfo uriInfo, ODataRequest request, OData odata, ServiceMetadata serviceMetadata) throws ODataApplicationException {

    }

    private UriInfoContext getUriInfoContext(UriInfo uriInfo) throws ODataApplicationException {
        UriInfoResource uriInfoResource = uriInfo.asUriInfoResource();
        List<UriResource> uriResources = uriInfoResource.getUriResourceParts();
        UriInfoContext context = new UriInfoContext();
        for (int i = 0; i < uriResources.size(); i++) {
            UriResource uriResource = uriResources.get(i);
            ExpandOption expandOption = uriInfo.getExpandOption() != null && i == uriResources.size() - 1 ? uriInfo.getExpandOption() : new ExpandOptionImpl();
            if (uriResource.getKind() == UriResourceKind.entitySet) {
                UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriResource;
                context.edmEntityType = (EdmEntityType) uriResourceEntitySet.getTypeFilterOnCollection();
                context.entitySet = uriResourceEntitySet.getEntitySet();
                if (uriResourceEntitySet.getKeyPredicates() == null || uriResourceEntitySet.getKeyPredicates().size() == 0)
                    break;
                context.edmEntityType = (EdmEntityType) uriResourceEntitySet.getTypeFilterOnEntry();
                context.keyParams = uriResourceEntitySet.getKeyPredicates();
                context.entity = retrieveEntity(JsonObj.parse(context.keyParams, context.getType()), expandOption);
            } else if (uriResource.getKind() == UriResourceKind.navigationProperty) {
                UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) uriResource;
                context.edmNavigationProperty = uriResourceNavigation.getProperty();
                context.edmEntityType = (EdmEntityType) uriResourceNavigation.getTypeFilterOnCollection();
                if (uriResourceNavigation.getKeyPredicates() == null || uriResourceNavigation.getKeyPredicates().size() == 0)
                    break;
                context.edmEntityType = (EdmEntityType) uriResourceNavigation.getTypeFilterOnEntry();
                context.keyParams = uriResourceNavigation.getKeyPredicates();
                JsonObj condition = JsonObj.parse(context.keyParams, context.getType());

                context.entity = retrieveEntity(condition, expandOption);
            }

        }
        return context;
    }

    public JsonObj createMediaEntity(UriInfo uriInfo, ODataRequest request, OData odata, ServiceMetadata serviceMetadata) throws ODataApplicationException {
        try {

            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List<FileItem> fileItems = upload.parseRequest(reqctx.getHttpServletRequest());
            FileItem propertiesItem = fileItems.stream().filter(item -> item.getFieldName().equals("properties")).findFirst().get();
            FileItem streamItem = fileItems.stream().filter(item -> item.getFieldName().equals("stream")).findFirst().get();
            String fileId = fs.write(streamItem.get());
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> properties = mapper.readValue(propertiesItem.get(), Map.class);
            properties.put("$stream", fileId);
            return createEntity(uriInfo, properties, odata, serviceMetadata);

        } catch (Exception e) {
            throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT, e);
        }
    }

    public byte[] readMediaEntity(UriInfo uriInfo, ODataRequest request, OData odata, ServiceMetadata serviceMetadata) throws ODataApplicationException {
        JsonObj entityObj = readEntity(uriInfo, odata, serviceMetadata);
        if (!entityObj.containsKey("$stream"))
            throw new ODataApplicationException("Media not found ", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
        String fileId = entityObj.get("$stream").toString();
        return fs.read(fileId);
    }

    class UriInfoContext {
        JsonObj entity;
        EdmEntityType edmEntityType;
        EdmEntitySet entitySet;
        List<UriParameter> keyParams;
        EdmNavigationProperty edmNavigationProperty;


        public EdmEntityType getType() {
            if (edmEntityType != null) return edmEntityType;
            return edmNavigationProperty == null ? entitySet.getEntityType() : edmNavigationProperty.getType();
        }


        public List<EdmEntityType> getDerivedTypes() throws ODataApplicationException {
            EdmEntityType sourceType = getType();
            List<EdmEntityType> derivedTypes = new ArrayList<>();
            try {
                Edm edm = (Edm) edmField.get(sourceType);
                for (EdmEntityType entityType : edm.getSchema(sourceType.getNamespace()).getEntityTypes()) {
                    if (isDerived(sourceType, entityType)) derivedTypes.add(entityType);
                }
                return derivedTypes;

            } catch (IllegalAccessException e) {
                throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.getDefault(), e);
            }
        }

        private boolean isDerived(EdmEntityType source, EdmEntityType target) throws ODataApplicationException {
            EdmEntityType type = target;
            while (type != null) {
                if (type.getFullQualifiedName().equals(source.getFullQualifiedName()))
                    return true;
                type = type.getBaseType();
            }

            return false;
        }
    }
}
