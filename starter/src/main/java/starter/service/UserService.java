package starter.service;


import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.rest.Json;
import starter.uContentException;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private RequestContext context;

//    public XContentBuilder all() throws IOException {
//        Client client = context.getClient();
//        //QueryBuilder queryBuilder = QueryBuilders.termQuery()
//        SearchResponse searchResponse = client.prepareSearch(context.getIndex()).setTypes(Constant.FieldName.USERTYPENAME).execute().actionGet();
//        SearchHits hits = searchResponse.getHits();
//        XContentBuilder builder= XContentFactory.jsonBuilder();
//        builder.startObject().field("total", searchResponse.getHits().totalHits());
//        builder.startArray("users");
//        for (SearchHit searchHitFields : searchResponse.getHits()) {
//            builder.startObject()
//                    .field("_id", searchHitFields.getId())
//                    .field(Constant.FieldName.USERID, searchHitFields.getSource().get(Constant.FieldName.USERID))
//                    .field(Constant.FieldName.USERNAME, searchHitFields.getSource().get(Constant.FieldName.USERNAME))
//                    .field(Constant.FieldName.EMAIL, searchHitFields.getSource().get(Constant.FieldName.EMAIL))
//                    .field(Constant.FieldName.PASSWORD, searchHitFields.getSource().get(Constant.FieldName.PASSWORD))
//                    .field(Constant.FieldName.CREATEDBY, searchHitFields.getSource().get(Constant.FieldName.CREATEDBY))
//                    .field(Constant.FieldName.CREATEDON, searchHitFields.getSource().get(Constant.FieldName.CREATEDON))
//                    .field(Constant.FieldName.LASTUPDATEDBY, searchHitFields.getSource().get(Constant.FieldName.LASTUPDATEDBY))
//                    .field(Constant.FieldName.LASTUPDATEDON, searchHitFields.getSource().get(Constant.FieldName.LASTUPDATEDON))
//                    .endObject();
//        }
//        builder.endArray();
//        builder.endObject();
//        //System.out.println(builder.string());
//        return builder;
//    }

    public XContentBuilder all(String query, int start, int limit, SortBuilder[] sort) throws IOException {
        Client client = context.getClient();
        SearchResponse searchResponse = null;
        if (limit>0){
            SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch(context.getIndex())
                    .setTypes(Constant.FieldName.USERTYPENAME).setFrom(start).setSize(limit);
            if (query != null && !query.isEmpty()) {
                searchRequestBuilder.setQuery(query);
            }
            if (sort != null && sort.length > 0) {
                for(SortBuilder sortBuilder : sort){
                    searchRequestBuilder.addSort(sortBuilder);
                }
            }
            searchResponse = searchRequestBuilder.execute().actionGet();
        }else{
            searchResponse = client.prepareSearch(context.getIndex()).setTypes(Constant.FieldName.USERTYPENAME).setQuery(query).execute().actionGet();
        }

        SearchHits hits = searchResponse.getHits();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject().field("total", searchResponse.getHits().totalHits());
        builder.startArray("users");
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            builder.startObject()
                    .field("_id", searchHitFields.getId())
                    .field(Constant.FieldName.USERID, searchHitFields.getSource().get(Constant.FieldName.USERID))
                    .field(Constant.FieldName.USERNAME, searchHitFields.getSource().get(Constant.FieldName.USERNAME))
                    .field(Constant.FieldName.EMAIL, searchHitFields.getSource().get(Constant.FieldName.EMAIL))
                    .field(Constant.FieldName.PASSWORD, searchHitFields.getSource().get(Constant.FieldName.PASSWORD))
                    .field(Constant.FieldName.CREATEDBY, searchHitFields.getSource().get(Constant.FieldName.CREATEDBY))
                    .field(Constant.FieldName.CREATEDON, searchHitFields.getSource().get(Constant.FieldName.CREATEDON))
                    .field(Constant.FieldName.LASTUPDATEDBY, searchHitFields.getSource().get(Constant.FieldName.LASTUPDATEDBY))
                    .field(Constant.FieldName.LASTUPDATEDON, searchHitFields.getSource().get(Constant.FieldName.LASTUPDATEDON))
                    .endObject();
        }
        builder.endArray();
        builder.endObject();
        System.out.println(builder.string());
        return builder;
    }

    public XContentBuilder create(Json body) throws IOException {
        Client client = context.getClient();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        body.put(Constant.FieldName.CREATEDBY, context.getUserName());
        body.put(Constant.FieldName.CREATEDON, new Date());
        IndexResponse indexResponse = client.prepareIndex(context.getIndex(), Constant.FieldName.USERTYPENAME).setSource(body).execute().actionGet();
        builder.startObject()
                .field("_index", indexResponse.getIndex())
                .field("_type", indexResponse.getType())
                .field("_id", indexResponse.getId())
                .field("_version", indexResponse.getVersion())
                .field("created", indexResponse.isCreated())
                .endObject();
        System.out.println(builder.string());
        return builder;
    }

    public XContentBuilder get(String id) throws IOException {
        Client client = context.getClient();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.USERTYPENAME, id).execute().actionGet();
        Map<String, Object> source = getResponse.getSource();
        builder.startObject()
                .field("_id", id)
                .field(Constant.FieldName.USERID, source.get(Constant.FieldName.USERID))
                .field(Constant.FieldName.USERNAME, source.get(Constant.FieldName.USERNAME))
                .field(Constant.FieldName.EMAIL, source.get(Constant.FieldName.EMAIL))
                .field(Constant.FieldName.PASSWORD, source.get(Constant.FieldName.PASSWORD))
                .field(Constant.FieldName.CREATEDBY, source.get(Constant.FieldName.CREATEDBY))
                .field(Constant.FieldName.CREATEDON, source.get(Constant.FieldName.CREATEDON))
                .field(Constant.FieldName.LASTUPDATEDBY, source.get(Constant.FieldName.LASTUPDATEDBY))
                .field(Constant.FieldName.LASTUPDATEDON, source.get(Constant.FieldName.LASTUPDATEDON))
                .endObject();
        System.out.println(builder.string());
        return builder;
    }

    public XContentBuilder ifUserExist(String userId) throws IOException {
        Client client = context.getClient();
        QueryBuilder queryBuilder = QueryBuilders.matchQuery(Constant.FieldName.USERID, userId);
        SearchResponse searchResponse = client.prepareSearch(context.getIndex()).setTypes(Constant.FieldName.USERTYPENAME).setQuery(queryBuilder).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        if(searchResponse.getHits().totalHits()>0){
            builder.startObject().field("exist", true).endObject();
        }else{
            builder.startObject().field("exist", false).endObject();
        }
        System.out.println(builder.string());
        return builder;
    }

    public XContentBuilder update(String id, Json body) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.USERTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }
        body.put(Constant.FieldName.LASTUPDATEDBY, context.getUserName());
        body.put(Constant.FieldName.LASTUPDATEDON, new Date());
        UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), Constant.FieldName.USERTYPENAME, id).setDoc(body).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", context.getIndex())
                .field("_type", Constant.FieldName.USERTYPENAME)
                .field("_id", id)
                .field("_version", updateResponse.getVersion())
                .field("_isCreated", updateResponse.isCreated())
                .endObject();
        return builder;
    }

    public XContentBuilder delete(String id) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.USERTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }
        DeleteResponse deleteResponse = client.prepareDelete(context.getIndex(), Constant.FieldName.USERTYPENAME, id).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", context.getIndex())
                .field("_type", Constant.FieldName.USERTYPENAME)
                .field("_id", id)
                .field("_version", deleteResponse.getVersion())
                .field("found", deleteResponse.isFound())
                .endObject();
        return builder;
    }

    public XContentBuilder getGroups(String id) throws IOException {
        Client client = context.getClient();
        QueryBuilder queryBuilder = QueryBuilders.matchQuery(Constant.FieldName.USERID, id);
        SearchResponse searchResponse = client.prepareSearch(context.getIndex()).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(queryBuilder).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject().field("total", searchResponse.getHits().totalHits());
        builder.startObject("groups");
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            System.out.println(searchHitFields.getId()+"=="+
                    searchHitFields.getSource().get("groupName")+"=="+
                    searchHitFields.getSource().get(Constant.FieldName.CREATEDBY)+"=="+
                    searchHitFields.getSource().get(Constant.FieldName.CREATEDON));

            builder.startObject()
                    .field("_id", searchHitFields.getId())
                    .field("groupName", searchHitFields.getSource().get("groupName"))
                    .field("users", searchHitFields.getSource().get("users"))
                    .field(Constant.FieldName.CREATEDBY, searchHitFields.getSource().get(Constant.FieldName.CREATEDBY))
                    .field(Constant.FieldName.CREATEDON, searchHitFields.getSource().get(Constant.FieldName.CREATEDON))
                    .field(Constant.FieldName.LASTUPDATEDBY, searchHitFields.getSource().get(Constant.FieldName.LASTUPDATEDBY))
                    .field(Constant.FieldName.LASTUPDATEDON, searchHitFields.getSource().get(Constant.FieldName.LASTUPDATEDON))
                    .endObject();
        }
        builder.endObject();
        System.out.println(builder.string());

        return builder;
    }

    public void initialUserMapping() throws IOException {
        Client client = context.getClient();
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(context.getIndex()).addTypes(Constant.FieldName.USERTYPENAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if(mappings.size()==0){
            //冇得，那就搞一个吧。。。
            XContentBuilder builder= XContentFactory.jsonBuilder();
            builder.startObject();
            builder.startObject(Constant.FieldName.USERTYPENAME);
            builder.startObject("properties")
                    .startObject(Constant.FieldName.USERID).field("type", "string").field("store", "yes").endObject()
                    .startObject(Constant.FieldName.USERNAME).field("type", "string").field("store", "yes").endObject()
                    .startObject(Constant.FieldName.EMAIL).field("type", "string").field("store", "yes").endObject()
                    .startObject(Constant.FieldName.PASSWORD).field("type", "string").field("store", "yes").endObject()
                    .startObject(Constant.FieldName.CREATEDBY).field("type", "string").field("store", "yes").endObject()
                    .startObject(Constant.FieldName.CREATEDON).field("type", "date").field("store", "yes").endObject()
                    .startObject(Constant.FieldName.LASTUPDATEDBY).field("type", "string").field("store", "yes").endObject()
                    .startObject(Constant.FieldName.LASTUPDATEDON).field("type", "date").field("store", "yes").endObject();
            builder.endObject();//end of typeName
            builder.endObject();
            //创建mapping
            PutMappingRequest mapping = Requests.putMappingRequest(context.getIndex()).type(Constant.FieldName.USERTYPENAME).source(builder);
            PutMappingResponse putMappingResponse = client.admin().indices().putMapping(mapping).actionGet();
        }else{
            //艹，居然有！！！！！
        }
    }

}
