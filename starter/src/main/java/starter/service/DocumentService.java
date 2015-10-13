package starter.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectCursor;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.LocalDateTime;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import starter.RequestContext;
import starter.rest.Json;
import starter.service.fs.FileSystem;
import starter.uContentException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

@Service
public class DocumentService {

    @Autowired
    private RequestContext context;

    @Autowired
    private FileSystem fs;

    @Autowired
    private TypeService typeService;

    @Autowired
    private UserService userService;

    private Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private Set<String> getFulltextProperties(String[] types) throws IOException {
        Set<String> keys = new HashSet<>();
        GetMappingsResponse response = context.getClient().admin().indices().prepareGetMappings(context.getIndex()).setTypes(types).execute().actionGet();
        ImmutableOpenMap<String, MappingMetaData> map = response.getMappings().get(context.getIndex());
        for (ObjectCursor<String> key : map.keys()) {
            //keys.addAll(((Map) map.get(key.value).getSourceAsMap().get("properties")).keySet());
            Map properties = (Map) map.get(key.value).getSourceAsMap().get("properties");
            for (Object name : properties.keySet()) {
                Map attribute = (Map) properties.get(name);
                if ("string".equals(attribute.get("type"))) {
                    keys.add(name.toString());
                }
            }
        }
        keys.add("_fullText");
        return keys;
    }


    public XContentBuilder query(String[] types, String query, int start, int limit, SortBuilder[] sort, boolean allowableActions, boolean fulltext) throws IOException {
        SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch(context.getIndex()).setFrom(start).setSize(limit);
        //set types
        if (types == null || types.length == 0) {
            types = typeService.getAllTypes().toArray(new String[]{});
        }
        searchRequestBuilder.setTypes(types);
        //set query
        if (StringUtils.isNotBlank(query)) {
            if (fulltext) {
                Set<String> keys = getFulltextProperties(types);
                BoolQueryBuilder booleanBuilder = QueryBuilders.boolQuery();
                for (String key : keys) {
                    searchRequestBuilder.addHighlightedField(key);
                    booleanBuilder.should(QueryBuilders.matchQuery(key, query));
                }
//                query = booleanBuilder.toString();
                searchRequestBuilder.setQuery(booleanBuilder);
            } else {
                searchRequestBuilder.setQuery(query);
            }
        }
        //set sort
        if (sort != null && sort.length > 0) {
            for (SortBuilder sortBuilder : sort) {
                searchRequestBuilder.addSort(sortBuilder);
            }
        }

        //_fullText field not return
        String[] exclude = {"_streams._fullText"};
        searchRequestBuilder.setFetchSource(null, exclude);

        BoolFilterBuilder filter = FilterBuilders.boolFilter();
        TermFilterBuilder userFilter = FilterBuilders.termFilter("_acl.read.users", context.getUserName());
        filter.should(userFilter);
        List<String> groups = userService.getGroupsOfUser(context.getUserName());
        for(String group : groups){
            TermFilterBuilder groupFilter = FilterBuilders.termFilter("_acl.read.groups", group);
            filter.should(groupFilter);
        }
//        if (StringUtils.isNotBlank(query)) {
            searchRequestBuilder.setQuery(toFilteredQuery(query, filter.toXContent(JsonXContent.contentBuilder(), ToXContent.EMPTY_PARAMS).string()));
//        }else{
//            searchRequestBuilder.setPostFilter(filter);
//        }
        //process result
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
        xContentBuilder.field("total", searchResponse.getHits().getTotalHits());
        xContentBuilder.startArray("documents");
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            xContentBuilder.startObject();
            xContentBuilder.field("_index", hit.getIndex())
                    .field("_type", hit.getType())
                    .field("_id", hit.getId())
                    .field("_score", hit.getScore())
                    .field("_version", hit.getVersion())
                    .field("_highlight", hit.getHighlightFields());
            Map<String, Object> source = hit.getSource();
            Iterator<Map.Entry<String, Object>> iterator = source.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                xContentBuilder.field(entry.getKey(), entry.getValue());
            }
            if (allowableActions) {
                xContentBuilder.field(Constant.FieldName.ALLOWABLEACTIONS, getUserPermission(context.getUserName(), source.get("_acl")));
            }
            xContentBuilder.endObject();
        }
        xContentBuilder.endArray().endObject();
        return xContentBuilder;
    }

    public XContentBuilder create(String type, Json body) throws IOException, ParseException {
        processAcl(body);
        validate(body, type);
        beforeCreate(body);
        IndexResponse indexResponse = context.getClient().prepareIndex(context.getIndex(), type).setSource(body).execute().actionGet();
        XContentBuilder builder = JsonXContent.contentBuilder();
        builder.startObject()
                .field("_index", indexResponse.getIndex())
                .field("_type", indexResponse.getType())
                .field("_id", indexResponse.getId())
                .field("_version", indexResponse.getVersion())
                .field("_created", indexResponse.isCreated())
                .endObject();
        return builder;
    }

    private void processAcl(Json body) {
        Object o = body.get("_acl");
        if (o != null && StringUtils.isNotBlank(o.toString())) {
            if (o instanceof Map) {
                return;
            }
            if(o instanceof String){
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    Map<String, Object> acl = objectMapper.readValue(o.toString(), Map.class);
                    body.put("_acl", acl);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }else{
            body.remove("_acl");
        }
    }

    public XContentBuilder create(String type, Json body, List<MultipartFile> files) throws IOException, ParseException {
        if (!files.isEmpty()) {
            List<Map<String, Object>> streams = new ArrayList<Map<String, Object>>();
            for (MultipartFile file : files) {
                Map<String, Object> stream = new HashMap<String, Object>();
                String fileId = fs.write(file.getBytes());
                if (StringUtils.isBlank(fileId)) {
                    logger.error(String.format("The stream: %s store failed", file.getName()));
                    throw new uContentException("FS store failed", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                stream.put(Constant.FieldName.STREAMID, fileId);
                stream.put(Constant.FieldName.STREAMNAME, file.getOriginalFilename());
                stream.put(Constant.FieldName.LENGTH, file.getSize());
                stream.put(Constant.FieldName.CONTENTTYPE, file.getContentType());
                stream.put(Constant.FieldName.FULLTEXT, parse(file.getInputStream()));
                if ("image/tiff".equalsIgnoreCase(file.getContentType())) {
                    ImageReader reader = ImageIO.getImageReadersByFormatName("tif").next();
                    reader.setInput(ImageIO.createImageInputStream(file.getInputStream()));
                    int pageCount = reader.getNumImages(true);
                    stream.put(Constant.FieldName.PAGECOUNT, pageCount);
                }
                streams.add(stream);
            }
            body.put(Constant.FieldName.STREAMS, streams);
        }
        return create(type, body);
    }


    public Json get(String type, String id, boolean head, boolean allowableActions) throws IOException {
        GetResponse getResponse = checkPermission(type, id, context.getUserName(), Constant.Permission.read);
        return processGet(getResponse, head, allowableActions);
    }


    public XContentBuilder update(String type, String id, Json body, List<MultipartFile> files) throws IOException, ParseException {
        GetResponse getResponse = checkPermission(type, id, context.getUserName(), Constant.Permission.write);
        List<Map<String, Object>> streams = new ArrayList<Map<String, Object>>();
        Object _streams = getResponse.getSource().get("_streams");
        if (_streams != null) {
            List<Map<String, Object>> oldSteams = (List<Map<String, Object>>) _streams;
            Object o = body.get("_removeStreamIds");
            if (o != null && StringUtils.isNotBlank(o.toString())) {
                String[] split = o.toString().split(",");
                List<String> deleteList = new ArrayList<>();
                Collections.addAll(deleteList, split);
                Iterator<Map<String, Object>> iterator = oldSteams.iterator();
                while (iterator.hasNext()){
                    String streamId = iterator.next().get("streamId").toString();
                    if(deleteList.contains(streamId)){
                        iterator.remove();
                        continue;
                    }
                }
            }
            streams.addAll(oldSteams);
        }
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                Map<String, Object> stream = new HashMap<String, Object>();
                String fileId = fs.write(file.getBytes());
                if (StringUtils.isBlank(fileId)) {
                    logger.error(String.format("The stream: %s store failed", file.getName()));
                    throw new uContentException("FS store failed", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                stream.put(Constant.FieldName.STREAMID, fileId);
                stream.put(Constant.FieldName.STREAMNAME, file.getOriginalFilename());
                stream.put(Constant.FieldName.LENGTH, file.getSize());
                stream.put(Constant.FieldName.CONTENTTYPE, file.getContentType());
                stream.put(Constant.FieldName.FULLTEXT, parse(file.getInputStream()));
                if ("image/tiff".equalsIgnoreCase(file.getContentType())) {
                    ImageReader reader = ImageIO.getImageReadersByFormatName("tif").next();
                    reader.setInput(ImageIO.createImageInputStream(file.getInputStream()));
                    int pageCount = reader.getNumImages(true);
                    stream.put(Constant.FieldName.PAGECOUNT, pageCount);
                }
                streams.add(stream);
            }
        }
        body.put(Constant.FieldName.STREAMS, streams);
        processAcl(body);
        validate(body, type);
        beforeUpdate(body);
        UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), type, id).setDoc(body).execute().actionGet();
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
        xContentBuilder.startObject()
                .field("_index", context.getIndex())
                .field("_type", type)
                .field("_id", id)
                .field("_version", updateResponse.getVersion());
        xContentBuilder.endObject();
        return xContentBuilder;
    }


    public XContentBuilder delete(String type, String id) throws IOException {
        checkPermission(type, id, context.getUserName(), Constant.Permission.write);
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
        xContentBuilder.startObject()
                .field("_index", context.getIndex())
                .field("_type", type)
                .field("_id", id);
        DeleteResponse deleteResponse = context.getClient().prepareDelete(context.getIndex(), type, id).execute().actionGet();
        xContentBuilder.field("_version", deleteResponse.getVersion());
        xContentBuilder.field("_found", deleteResponse.isFound());
        xContentBuilder.endObject();
        return xContentBuilder;
    }

    public XContentBuilder delete(List<Map> body){
        XContentBuilder xContentBuilder = null;
        try {
            xContentBuilder = JsonXContent.contentBuilder().startArray();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        for(Map<String, Object> map : body){
            String type = map.get("type").toString();
            String id = map.get("id").toString();
            try {
                checkPermission(type, id, context.getUserName(), Constant.Permission.write);
                DeleteResponse deleteResponse = context.getClient().prepareDelete(context.getIndex(), type, id).execute().actionGet();
                xContentBuilder.startObject().field("_index", context.getIndex())
                        .field("_type", type)
                        .field("_id", id)
                        .field("delete", deleteResponse.isFound()).endObject();
            } catch (Exception e) {
                logger.error(e.getMessage());
                try {
                    xContentBuilder.startObject().field("_index", context.getIndex())
                            .field("_type", type)
                            .field("_id", id)
                            .field("delete", e.getMessage()).endObject();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        try {
            xContentBuilder.endArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xContentBuilder;
    }


    private void beforeCreate(Json body) throws IOException {
        LocalDateTime localDateTime = new DateTime().toLocalDateTime();
        body.put(Constant.FieldName.CREATEDBY, context.getUserName());
        body.put(Constant.FieldName.CREATEDON, localDateTime);
//        body.put(Constant.FieldName.LASTUPDATEDBY, context.getUserName());
//        body.put(Constant.FieldName.LASTUPDATEDON, localDateTime);

        Map<String, Object> acl = new HashMap<>();
        Object o = body.get(Constant.FieldName.ACL);
        if (o == null) {
            List<String> users = new ArrayList<>();
            users.add(context.getUserName());
            Map<String, List<String>> read = new HashMap<>();
            read.put("users", users);
            Map<String, List<String>> write = new HashMap<>();
            write.put("users", users);
            acl.put("read", read);
            acl.put("write", write);
            body.put(Constant.FieldName.ACL, acl);
        }
    }


    private void validateAcl(Map<String, Object> acl) {
        Iterator<Map.Entry<String, Object>> it = acl.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, Object> pEntry = it.next();
            String pKey = pEntry.getKey();
            if (!Constant.Permission.getPermissionDeclaration().contains(pKey)) {
                it.remove();
                continue;
            }
            Map<String, Object> entry = (Map<String, Object>) pEntry.getValue();
            Iterator<Map.Entry<String, Object>> iterator = entry.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, Object> en = iterator.next();
                String key = en.getKey();
                if (!key.equals("users") && !key.equals("groups")) {
                    iterator.remove();
                    continue;
                }
            }
        }
    }

    private Json processGet(GetResponse getResponse, boolean head, boolean allowableActions) throws IOException {
        Json json = new Json();
        json.put("_index", getResponse.getIndex());
        json.put("_type", getResponse.getType());
        json.put("_id", getResponse.getId());
        json.put("_found", getResponse.isExists());
        if (getResponse.isExists()) {
            json.put("_version", getResponse.getVersion());
            if (!head) {
                Map<String, Object> source = getResponse.getSource();
                if (source != null) {
                    Iterator<Map.Entry<String, Object>> iterator = source.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Object> entry = iterator.next();
                        json.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            if (allowableActions) {
                json.put(Constant.FieldName.ALLOWABLEACTIONS, getUserPermission(context.getUserName(), getResponse.getSource().get(Constant.FieldName.ACL)));
            }
        }
        return json;
    }

    private Set getUserPermission(String user, Object acl) throws IOException {
        Map<String, Object> _acl = (Map<String, Object>) acl;
        Set uPermission = getPermissionByUser(user, _acl);
        List<String> groups = getGroups(user);
        Set gPermission = getPermissionByGroups(groups, _acl);
        uPermission.addAll(gPermission);
        return uPermission;
    }

    private List<String> getGroups(String user) throws IOException {
        return userService.getGroupsOfUser(user);
    }

    private Set getPermissionByUser(String user, Map<String, Object> acl) {
        Set permission = new HashSet();
        if (acl != null && !acl.isEmpty()) {
            Iterator<Map.Entry<String, Object>> it = acl.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String, Object> entry = it.next();
                Map<String, Object> map = (Map<String, Object>) entry.getValue();
                Object users = map.get("users");
                if (users != null) {
                    List<String> _users = (List<String>) users;
                    if(_users.contains(user)){
                        permission.add(entry.getKey());
                        continue;
                    }
                }
            }
        }
        return permission;
    }

    private Set getPermissionByGroups(List<String> groups, Map<String, Object> acl) {
        Set permission = new HashSet();
        if (groups == null || groups.isEmpty()) {
            return permission;
        }
        if (acl != null && !acl.isEmpty()) {
            Iterator<Map.Entry<String, Object>> it = acl.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String, Object> entry = it.next();
                Map<String, Object> map = (Map<String, Object>) entry.getValue();
                Object g = map.get("groups");
                if (g != null) {
                    List<String> _groups = (List<String>) g;
                    for(String s : groups){
                        if (_groups.contains(s)) {
                            permission.add(entry.getKey());
                            continue;
                        }
                    }
                }
            }
        }
        return permission;
    }

    private boolean hasPermission(String user, Object acl, Constant.Permission action) throws IOException {
        Map<String, Object> _acl = (Map<String, Object>) acl;
        Set permission = getPermissionByUser(user, _acl);
        if (permission.contains(action.toString())) {
            return true;
        } else {
            List<String> groups = getGroups(user);
            permission = getPermissionByGroups(groups, _acl);
            return permission.contains(action.toString());
        }
    }

    private void beforeUpdate(Json body) {
        if (body != null) {
            LocalDateTime localDateTime = new DateTime().toLocalDateTime();
            body.put(Constant.FieldName.LASTUPDATEDBY, context.getUserName());
            body.put(Constant.FieldName.LASTUPDATEDON, localDateTime);
        }
    }


    public GetResponse checkPermission(String type, String id, String user, Constant.Permission permission) throws IOException {
        String[] exclude = {"_streams._fullText"};
        GetResponse getResponse = context.getClient().prepareGet(context.getIndex(), type, id).setFetchSource(null, exclude).execute().actionGet();
        if (!getResponse.isExists()) {
            logger.warn(String.format("The doc: %s in type %s of index %s is not exist", id, type, context.getIndex()));
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }
        if (!hasPermission(user, getResponse.getSource().get(Constant.FieldName.ACL), permission)) {
            logger.warn(String.format("The user: %s do not have the permission: %s on doc %s", user, permission, id));
            throw new uContentException("Forbidden", HttpStatus.FORBIDDEN);
        }
        return getResponse;
    }

    private void validate(Json body, String type) throws IOException, ParseException {
        Map<String, Map<String, Object>> definition = typeService.getProperties(type);
        Set<String> keySet = definition.keySet();
        Iterator<Map.Entry<String, Object>> iterator = body.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            if (key.equals(Constant.FieldName.STREAMS)) {
                continue;
            }
            if (key.equals(Constant.FieldName.ACL)) {
                validateAcl((Map<String, Object>)entry.getValue());
                continue;
            }
            if(key.equals("createdBy") || key.equals("createdOn") || key.equals("lastUpdatedBy") || key.equals("lastUpdatedOn")){
                iterator.remove();
                continue;
            }
            if (!keySet.contains(key)) {//ignore undefined getFulltextProperties
                logger.warn(String.format("The getFulltextProperties: %s has not defined, Ignore!", key));
                iterator.remove();
                continue;
            }
            Map<String, Object> property = definition.get(key);
            String propType = property.get(Constant.FieldName.TYPE).toString();
            entry.setValue(formatValue(propType, entry.getValue()));
        }
        Iterator<Map<String, Object>> it = definition.values().iterator();
        while (it.hasNext()) {
            Map<String, Object> entry = it.next();
            if ((Boolean) entry.get(Constant.FieldName.REQUIRED) == true) {
                String propName = entry.get(Constant.FieldName.NAME).toString();
                Object v = body.get(propName);
                if (v == null) {
                    Object defaultValue = entry.get(Constant.FieldName.DEFAULTVALUE);
                    if (defaultValue == null || defaultValue.toString().equals("")) {
                        logger.error(String.format("Property : %s is required", propName));
                        throw new uContentException(String.format("Property : %s is required", propName), HttpStatus.BAD_REQUEST);
                    }
                    body.put(propName, formatValue(entry.get(Constant.FieldName.TYPE).toString(), defaultValue));
                }
            }
        }
    }

    private Object formatValue(String type, Object value) throws ParseException {
        if (value == null || value.toString().equals("")) {
            return null;
        }
        String StringValue = value.toString();
        switch (type) {
            case Constant.Type.INTEGER:
                return Integer.valueOf(StringValue);
            case Constant.Type.FLOAT:
                return Float.valueOf(StringValue);
            case Constant.Type.DATE:
                return DateTime.parse(StringValue).toLocalDateTime();
            case Constant.Type.BOOLEAN:
                return Boolean.valueOf(StringValue);
            default:
                return StringValue;
        }
    }

    public String parse(InputStream in) {
        try {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            parser.parse(in, handler, metadata);
            return handler.toString();
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (SAXException e) {
            logger.error(e.getMessage());
        } catch (TikaException e) {
            logger.error(e.getMessage());
        } finally {
            IOUtils.closeQuietly(in);
        }
        return "";
    }

    private String toFilteredQuery(String query, String filter){
        if (StringUtils.isNotBlank(query)) {
            return "{\"filtered\":{\"query\":" + query + ",\"filter\":" + filter + "}}";
        }
        return "{\"filtered\":{\"filter\":" + filter + "}}";
    }

}
