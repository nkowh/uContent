package starter.service;


import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.rest.Json;
import starter.uContentException;

import java.io.IOException;
import java.util.*;

@Service
public class GroupService {

    private static final String userTypeName = "user";

    private static final String groupTypeName = "group";

    @Autowired
    private RequestContext context;

    public XContentBuilder all() throws IOException {
        Client client = context.getClient();
        //QueryBuilder queryBuilder = QueryBuilders.termQuery()
        SearchResponse searchResponse = client.prepareSearch(context.getIndex()).setTypes(groupTypeName).execute().actionGet();
        SearchHits hits = searchResponse.getHits();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject();
        builder.startArray("groups");
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            builder.startObject()
                    .field("_id", searchHitFields.getId())
                    .field("groupName", searchHitFields.getSource().get("groupName"))
                    .field("users", searchHitFields.getSource().get("users"))
                    .field("createBy", searchHitFields.getSource().get("createBy"))
                    .field("creationDate", searchHitFields.getSource().get("creationDate"))
                    .field("lastModifiedBy", searchHitFields.getSource().get("lastModifiedBy"))
                    .field("lastModificationDate", searchHitFields.getSource().get("lastModificationDate"))
                    .endObject();
        }
        builder.endArray();
        builder.endObject();
        //System.out.println(builder.string());
        return builder;
    }

    public XContentBuilder all(Json query, int start, int limit, String sort, String sord) throws IOException {
        Client client = context.getClient();
        //QueryBuilder queryBuilder = QueryBuilders.termQuery()
        //SearchResponse searchResponse = client.prepareSearch(context.getIndex()).setTypes(groupTypeName).execute().actionGet();

        SearchResponse searchResponse = null;
        if (limit>0){
            SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch(context.getIndex())
                    .setTypes(groupTypeName).setFrom(start).
                            setSize(limit).addSort(sort, sord.equalsIgnoreCase("asc")? SortOrder.ASC:SortOrder.DESC);
            if (query != null && !query.isEmpty()) {
                searchRequestBuilder.setQuery(query);
            }
            searchResponse = searchRequestBuilder.execute().actionGet();
        }else{
            searchResponse = client.prepareSearch(context.getIndex()).setTypes(groupTypeName).execute().actionGet();
        }

        SearchHits hits = searchResponse.getHits();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject().field("total", searchResponse.getHits().totalHits());
        builder.startArray("groups");
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            builder.startObject()
                    .field("_id", searchHitFields.getId())
                    .field("groupName", searchHitFields.getSource().get("groupName"))
                    .field("users", searchHitFields.getSource().get("users"))
                    .field("createBy", searchHitFields.getSource().get("createBy"))
                    .field("creationDate", searchHitFields.getSource().get("creationDate"))
                    .field("lastModifiedBy", searchHitFields.getSource().get("lastModifiedBy"))
                    .field("lastModificationDate", searchHitFields.getSource().get("lastModificationDate"))
                    .endObject();
        }
        builder.endArray();
        builder.endObject();
        //System.out.println(builder.string());
        return builder;
    }

    public XContentBuilder create(Json body) throws IOException {
        Client client = context.getClient();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        body.put("createBy", context.getUserName());
        body.put("creationDate", new Date());
        IndexResponse indexResponse = client.prepareIndex(context.getIndex(), groupTypeName).setSource(body).execute().actionGet();
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
        GetResponse getResponse = client.prepareGet(context.getIndex(), groupTypeName, id).execute().actionGet();
        Map<String, Object> source = getResponse.getSource();
        builder.startObject()
                .field("_id", id)
                .field("groupName", source.get("groupName"))
                .field("users", source.get("users"))
                .field("createBy", source.get("createBy"))
                .field("creationDate", source.get("creationDate"))
                .field("lastModifiedBy", source.get("lastModifiedBy"))
                .field("lastModificationDate", source.get("lastModificationDate"))
                .endObject();
        System.out.println(builder.string());
        return builder;
    }

    public XContentBuilder getUsers(String id) throws IOException {
        Client client = context.getClient();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        GetResponse getResponse = client.prepareGet(context.getIndex(), groupTypeName, id).execute().actionGet();
        Map<String, Object> source = getResponse.getSource();
        builder.startObject();
        builder.startArray("users");
        ArrayList<HashMap<String, Object>> users = (ArrayList<HashMap<String, Object>>)source.get("users");
        if (users!=null){
            for (HashMap<String, Object> user:users){
                GetResponse getUserResponse = client.prepareGet(context.getIndex(), userTypeName, user.get("userId").toString()).execute().actionGet();
                Map<String, Object> userSource = getUserResponse.getSource();
                builder.startObject()
                        .field("_id", id)
                        .field("userId", userSource.get("userId"))
                        .field("userName", userSource.get("userName"))
                        .field("email", userSource.get("email"))
                        .field("password", userSource.get("password"))
                        .field("createBy", userSource.get("createBy"))
                        .field("creationDate", userSource.get("creationDate"))
                        .field("lastModifiedBy", userSource.get("lastModifiedBy"))
                        .field("lastModificationDate", userSource.get("lastModificationDate"))
                        .endObject();
            }
        }
        builder.endArray();
        builder.endObject();
        //System.out.println(builder.string());
        return builder;
    }

    public XContentBuilder update(String id, Json body) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(context.getIndex(), groupTypeName, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }
        body.put("lastModifiedBy", context.getUserName());
        body.put("lastModificationDate", new Date());
        UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), groupTypeName, id).setDoc(body).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", context.getIndex())
                .field("_type", groupTypeName)
                .field("_id", id)
                .field("_version", updateResponse.getVersion())
                .field("_isCreated", updateResponse.isCreated())
                .endObject();
        return builder;
    }

    public XContentBuilder delete(String id) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(context.getIndex(), groupTypeName, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }
        DeleteResponse deleteResponse = client.prepareDelete(context.getIndex(), groupTypeName, id).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", context.getIndex())
                .field("_type", groupTypeName)
                .field("_id", id)
                .field("_version", deleteResponse.getVersion())
                .field("found", deleteResponse.isFound())
                .endObject();
        return builder;
    }

    public XContentBuilder refUsers(List<String> userIds) {
        Client client = context.getClient();
        return null;
    }
}
