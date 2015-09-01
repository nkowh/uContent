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
import org.springframework.util.StringUtils;
import starter.RequestContext;
import starter.rest.Json;
import starter.uContentException;

import java.io.IOException;
import java.util.*;

@Service
public class GroupService {

    @Autowired
    private RequestContext context;

    public XContentBuilder all(String query, int start, int limit, SortBuilder[] sort) throws IOException {
        Client client = context.getClient();
        SearchResponse searchResponse = null;
        if (limit>0){
            SearchRequestBuilder searchRequestBuilder = context.getClient().prepareSearch(context.getIndex())
                    .setTypes(Constant.FieldName.GROUPTYPENAME).setFrom(start).setSize(limit);
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
            searchResponse = client.prepareSearch(context.getIndex()).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(query).execute().actionGet();
        }

        SearchHits hits = searchResponse.getHits();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject().field("total", searchResponse.getHits().totalHits());
        builder.startArray("groups");
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            builder.startObject()
                    .field("_id", searchHitFields.getId())
                    .field(Constant.FieldName.GROUPID, searchHitFields.getSource().get(Constant.FieldName.GROUPID))
                    .field(Constant.FieldName.GROUPNAME, searchHitFields.getSource().get(Constant.FieldName.GROUPNAME))
                    .field(Constant.FieldName.USERS, searchHitFields.getSource().get(Constant.FieldName.USERS))
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

        body.remove(Constant.FieldName._ID);
        validateGroup(body, "create", "");

        body.put(Constant.FieldName.CREATEDBY, context.getUserName());
        body.put(Constant.FieldName.CREATEDON, new Date());
        body.put(Constant.FieldName.LASTUPDATEDBY, null);
        body.put(Constant.FieldName.LASTUPDATEDON, null);
        IndexResponse indexResponse = client.prepareIndex(context.getIndex(), Constant.FieldName.GROUPTYPENAME).
                setId(body.get(Constant.FieldName.GROUPID).toString()).setSource(body).execute().actionGet();
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
        GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
        Map<String, Object> source = getResponse.getSource();
        builder.startObject()
                .field("_id", id)
                .field(Constant.FieldName.GROUPID, source.get(Constant.FieldName.GROUPID))
                .field(Constant.FieldName.GROUPNAME, source.get(Constant.FieldName.GROUPNAME))
                .field(Constant.FieldName.USERS, source.get(Constant.FieldName.USERS))
                .field(Constant.FieldName.CREATEDBY, source.get(Constant.FieldName.CREATEDBY))
                .field(Constant.FieldName.CREATEDON, source.get(Constant.FieldName.CREATEDON))
                .field(Constant.FieldName.LASTUPDATEDBY, source.get(Constant.FieldName.LASTUPDATEDBY))
                .field(Constant.FieldName.LASTUPDATEDON, source.get(Constant.FieldName.LASTUPDATEDON))
                .endObject();
        System.out.println(builder.string());
        return builder;
    }

    public XContentBuilder getUsers(String id) throws IOException {
        Client client = context.getClient();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
        Map<String, Object> source = getResponse.getSource();
        builder.startObject();
        builder.startArray(Constant.FieldName.USERS);
        ArrayList<Object> users = (ArrayList<Object>)source.get(Constant.FieldName.USERS);
        if (users!=null){
            for (Object user:users){
                GetResponse getUserResponse = client.prepareGet(context.getIndex(), Constant.FieldName.USERTYPENAME, user.toString()).execute().actionGet();
                if (getUserResponse.isExists()){
                    Map<String, Object> userSource = getUserResponse.getSource();
                    builder.startObject()
                            .field("_id", id)
                            .field(Constant.FieldName.USERID, userSource.get(Constant.FieldName.USERID))
                            .field(Constant.FieldName.USERNAME, userSource.get(Constant.FieldName.USERNAME))
                            .field(Constant.FieldName.EMAIL, userSource.get(Constant.FieldName.EMAIL))
                            .field(Constant.FieldName.PASSWORD, userSource.get(Constant.FieldName.PASSWORD))
                            .field(Constant.FieldName.CREATEDBY, userSource.get(Constant.FieldName.CREATEDBY))
                            .field(Constant.FieldName.CREATEDON, userSource.get(Constant.FieldName.CREATEDON))
                            .field(Constant.FieldName.LASTUPDATEDBY, userSource.get(Constant.FieldName.LASTUPDATEDBY))
                            .field(Constant.FieldName.LASTUPDATEDON, userSource.get(Constant.FieldName.LASTUPDATEDON))
                            .endObject();
                }
            }
        }
        builder.endArray();
        builder.endObject();
        System.out.println(builder.string());
        return builder;
    }

    public XContentBuilder update(String id, Json body) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }else{
            if (getResponse.getId().equals(Constant.ADMINGROUP)||getResponse.getId().equals(Constant.EVERYONE)){
                throw new uContentException("Can't Be Modified", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        body.remove(Constant.FieldName._ID);
        validateGroup(body, "update", id);

        body.remove(Constant.FieldName.CREATEDBY);
        body.remove(Constant.FieldName.CREATEDON);
        body.put(Constant.FieldName.LASTUPDATEDBY, context.getUserName());
        body.put(Constant.FieldName.LASTUPDATEDON, new Date());
        UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), Constant.FieldName.GROUPTYPENAME, id).setDoc(body).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", context.getIndex())
                .field("_type", Constant.FieldName.GROUPTYPENAME)
                .field("_id", id)
                .field("_version", updateResponse.getVersion())
                .field("_isCreated", updateResponse.isCreated())
                .endObject();
        return builder;
    }

    public XContentBuilder delete(String id) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }else{
            if (getResponse.getId().equals(Constant.ADMINGROUP)||getResponse.getId().equals(Constant.EVERYONE)){
                throw new uContentException("Can't Be Deleted", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        DeleteResponse deleteResponse = client.prepareDelete(context.getIndex(), Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", context.getIndex())
                .field("_type", Constant.FieldName.GROUPTYPENAME)
                .field("_id", id)
                .field("_version", deleteResponse.getVersion())
                .field("found", deleteResponse.isFound())
                .endObject();
        return builder;
    }

    public XContentBuilder refUsers(String id, Json userIds) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }

        //传入的users必须为数组
        Object users = userIds.get(Constant.FieldName.USERS);
        if (users!=null){
            if (users instanceof List){
                for(Object userOjb:(ArrayList<Object>)users){

                }
            }else{
                throw new uContentException("Bad Data", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        Map<String, Object> source = new HashMap<String, Object>();
        source.put(Constant.FieldName.USERS, users);
        source.put(Constant.FieldName.LASTUPDATEDBY, context.getUserName());
        source.put(Constant.FieldName.LASTUPDATEDON, new Date());
        UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), Constant.FieldName.GROUPTYPENAME, id).setDoc(source).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", context.getIndex())
                .field("_type", Constant.FieldName.GROUPTYPENAME)
                .field("_id", id)
                .field("_version", updateResponse.getVersion())
                .field("_isCreated", updateResponse.isCreated())
                .endObject();
        return builder;
    }

    public void initialGroupData() throws IOException {
        Client client = context.getClient();

        //创建group Mapping
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(context.getIndex()).addTypes(Constant.FieldName.GROUPTYPENAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if(mappings.size()==0){
            //冇得，那就搞一个吧。。。
            XContentBuilder builder= XContentFactory.jsonBuilder();
            builder.startObject();
            builder.startObject(Constant.FieldName.GROUPTYPENAME);
            builder.startObject("properties")
                    .startObject(Constant.FieldName.GROUPID).field("type", "string").field("store", "yes").endObject()
                    .startObject(Constant.FieldName.GROUPNAME).field("type", "string").field("store", "yes").endObject()
                    .startObject(Constant.FieldName.USERS).field("type", "string").field("store", "yes").endObject()
                    .startObject(Constant.FieldName.CREATEDBY).field("type", "string").field("store", "yes").endObject()
                    .startObject(Constant.FieldName.CREATEDON).field("type", "date").field("store", "yes").endObject()
                    .startObject(Constant.FieldName.LASTUPDATEDBY).field("type", "string").field("store", "yes").endObject()
                    .startObject(Constant.FieldName.LASTUPDATEDON).field("type", "date").field("store", "yes").endObject();
            builder.endObject();//end of typeName
            builder.endObject();
            //创建mapping
            PutMappingRequest mapping = Requests.putMappingRequest(context.getIndex()).type(Constant.FieldName.GROUPTYPENAME).source(builder);
            PutMappingResponse putMappingResponse = client.admin().indices().putMapping(mapping).actionGet();
        }else{
            //艹，居然有！！！！！
        }


        //创建ADMINGROUP
        if (!client.prepareGet(context.getIndex(), Constant.FieldName.GROUPTYPENAME, Constant.ADMINGROUP).execute().actionGet().isExists()) {
            Map<String, Object> adminGroup = new HashMap<String, Object>();
            adminGroup.put(Constant.FieldName.GROUPID, Constant.ADMINGROUP);
            adminGroup.put(Constant.FieldName.GROUPNAME, Constant.ADMINGROUP);
            List<String> users = new ArrayList<String>();
            users.add(Constant.ADMIN);
            adminGroup.put(Constant.FieldName.USERS, users);
            adminGroup.put(Constant.FieldName.CREATEDBY, Constant.ADMIN);
            adminGroup.put(Constant.FieldName.CREATEDON, new Date());
            adminGroup.put(Constant.FieldName.LASTUPDATEDBY, null);
            adminGroup.put(Constant.FieldName.LASTUPDATEDON, null);

            IndexResponse adminGroupResponse = client.prepareIndex(context.getIndex(), Constant.FieldName.GROUPTYPENAME
            ).setId(Constant.ADMINGROUP).setSource(adminGroup).execute().actionGet();
        }else{

        }

        //创建EVERYONE
        if (!client.prepareGet(context.getIndex(), Constant.FieldName.GROUPTYPENAME, Constant.EVERYONE).execute().actionGet().isExists()) {
            Map<String, Object> everyone = new HashMap<String, Object>();
            everyone.put(Constant.FieldName.GROUPID, Constant.EVERYONE);
            everyone.put(Constant.FieldName.GROUPNAME, Constant.EVERYONE);
            everyone.put(Constant.FieldName.USERS, new ArrayList<String>());
            everyone.put(Constant.FieldName.CREATEDBY, Constant.ADMIN);
            everyone.put(Constant.FieldName.CREATEDON, new Date());
            everyone.put(Constant.FieldName.LASTUPDATEDBY, null);
            everyone.put(Constant.FieldName.LASTUPDATEDON, null);

            IndexResponse everyOneResponse = client.prepareIndex(context.getIndex(), Constant.FieldName.GROUPTYPENAME
            ).setId(Constant.EVERYONE).setSource(everyone).execute().actionGet();
        }else{

        }
    }

    private void validateGroup(Json body, String action, String id) {
        //校验groupId groupName
        Object groupId = body.get(Constant.FieldName.GROUPID);
        Object groupName = body.get(Constant.FieldName.GROUPNAME);
        if (action.equals("create")){
            if (StringUtils.isEmpty(groupId)||StringUtils.isEmpty(groupName)){
                throw new uContentException("Can't Be Blank", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }else if(action.equals("update")){
            if (groupId.equals("")||groupName.equals("")){
                throw new uContentException("Can't Be Blank", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        Client client = context.getClient();
        //校验groupId是否存在
        if (!StringUtils.isEmpty(groupId)){
            QueryBuilder queryBuilder = QueryBuilders.matchQuery(Constant.FieldName.GROUPID, groupId);
            SearchResponse searchResponse = client.prepareSearch(context.getIndex()).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(queryBuilder).execute().actionGet();
            SearchHits searchHits = searchResponse.getHits();
            if (action.equals("create")){
                if (searchHits.totalHits()>0){
                    throw new uContentException("Exist", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }else if(action.equals("update")){
//                if (searchHits.totalHits()>0){
//                    for (SearchHit searchHitFields : searchHits) {
//                        if(!id.equals(searchHitFields.getId())){
//                            throw new uContentException("Exist", HttpStatus.INTERNAL_SERVER_ERROR);
//                        }
//                    }
//                }
                //修改时groupId不可被修改
                GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.GROUPTYPENAME, id).execute().actionGet();
                Map<String, Object> source = getResponse.getSource();
                if(!groupId.equals(source.get(Constant.FieldName.GROUPID))){
                    throw new uContentException("groupId can't be modified", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }

        //校验groupName是否存在
        if (!StringUtils.isEmpty(groupName)){
            QueryBuilder queryBuilder = QueryBuilders.matchQuery(Constant.FieldName.GROUPNAME, groupName);
            SearchResponse searchResponse = client.prepareSearch(context.getIndex()).setTypes(Constant.FieldName.GROUPTYPENAME).setQuery(queryBuilder).execute().actionGet();
            SearchHits searchHits = searchResponse.getHits();
            if (action.equals("create")){
                if (searchHits.totalHits()>0){
                    throw new uContentException("Exist", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }else if(action.equals("update")){
                if (searchHits.totalHits()>0){
                    //修改时发现与其他group的groupname相同
                    for (SearchHit searchHitFields : searchHits) {
                        if(!id.equals(searchHitFields.getId())){
                            throw new uContentException("Exist", HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    }
                }
            }
        }

        //校验是否有多余的属性
        Iterator<Map.Entry<String, Object>> iterator = body.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            if(!(key.equals(Constant.FieldName.GROUPID)||key.equals(Constant.FieldName.GROUPNAME)||key.equals(Constant.FieldName.USERS)||
                    key.equals(Constant.FieldName.CREATEDBY)||key.equals(Constant.FieldName.CREATEDON)||
                    key.equals(Constant.FieldName.LASTUPDATEDBY)||key.equals(Constant.FieldName.LASTUPDATEDON)||
                    key.equals(Constant.FieldName._ID)
            )){
                throw new uContentException("Bad Data", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        //校验users
        Object users = body.get(Constant.FieldName.USERS);
        if (users!=null){
            if (users instanceof List){
                for(Object userOjb:(ArrayList<Object>)users){

                }
            }else{
                throw new uContentException("Bad Data", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}

