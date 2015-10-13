package starter.service;


import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.rest.Json;
import starter.uContentException;

import java.io.IOException;
import java.util.*;

@Service
public class ViewService {

    @Autowired
    private RequestContext context;

    @Autowired
    private UserService userService;

    public void initialViewData() throws IOException {
        Client client = context.getClient();

        //创建View Mapping
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(context.getIndex()).addTypes(Constant.FieldName.VIEWTYPENAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if(mappings.size()==0){
            //冇得，那就搞一个吧。。。
            XContentBuilder builder= XContentFactory.jsonBuilder();
            builder.startObject();
            builder.startObject(Constant.FieldName.VIEWTYPENAME);
            builder.startObject("properties")
                    //.startObject(Constant.FieldName.VIEWID).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.VIEWNAME).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.QUERYCONTEXT).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.USERS).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.GROUPS).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject();
                    //.startObject(Constant.FieldName.CREATEDBY).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    //.startObject(Constant.FieldName.CREATEDON).field("type", "date").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    //.startObject(Constant.FieldName.LASTUPDATEDBY).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    //.startObject(Constant.FieldName.LASTUPDATEDON).field("type", "date").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject();
            builder.endObject();//end of typeName
            builder.endObject();
            //创建mapping
            PutMappingRequest mapping = Requests.putMappingRequest(context.getIndex()).type(Constant.FieldName.VIEWTYPENAME).source(builder);
            PutMappingResponse putMappingResponse = client.admin().indices().putMapping(mapping).actionGet();
        }else{
            //艹，居然有！！！！！
        }
    }

    public List<Json> all() throws IOException {
        Client client = context.getClient();
        SearchResponse searchResponse = client.prepareSearch(context.getIndex()).setTypes(Constant.FieldName.VIEWTYPENAME).execute().actionGet();
        SearchHits hits = searchResponse.getHits();
        List<Json> views = new ArrayList<Json>();
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            Json view = new Json();
            view.put(Constant.FieldName._ID, searchHitFields.getId());
            view.put(Constant.FieldName.VIEWNAME, searchHitFields.getSource().get(Constant.FieldName.VIEWNAME));
            view.put(Constant.FieldName.QUERYCONTEXT, searchHitFields.getSource().get(Constant.FieldName.QUERYCONTEXT));
            view.put(Constant.FieldName.USERS, searchHitFields.getSource().get(Constant.FieldName.USERS));
            view.put(Constant.FieldName.GROUPS, searchHitFields.getSource().get(Constant.FieldName.GROUPS));
            views.add(view);
        }
        return views;
    }

    public XContentBuilder create(Json body) throws IOException {
        Client client = context.getClient();
        XContentBuilder builder= XContentFactory.jsonBuilder();

        body.remove(Constant.FieldName._ID);
        //body.put(Constant.FieldName.CREATEDBY, context.getUserName());
        //body.put(Constant.FieldName.CREATEDON, new Date());
        //body.put(Constant.FieldName.LASTUPDATEDBY, null);
        //body.put(Constant.FieldName.LASTUPDATEDON, null);
        IndexResponse indexResponse = client.prepareIndex(context.getIndex(), Constant.FieldName.VIEWTYPENAME).setSource(body).execute().actionGet();
        builder.startObject()
                .field("_index", indexResponse.getIndex())
                .field("_type", indexResponse.getType())
                .field("_id", indexResponse.getId())
                .field("_version", indexResponse.getVersion())
                .field("created", indexResponse.isCreated())
                .endObject();
        return builder;
    }

    public Json get(String id){
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.VIEWTYPENAME, id).execute().actionGet();
        Map<String, Object> source = getResponse.getSource();
        if (getResponse.isExists()){
            Json json = new Json();
            json.put(Constant.FieldName._ID, id);
            //json.put(Constant.FieldName.VIEWID, source.get(Constant.FieldName.VIEWID));
            json.put(Constant.FieldName.VIEWNAME, source.get(Constant.FieldName.VIEWNAME));
            json.put(Constant.FieldName.USERS, source.get(Constant.FieldName.USERS));
            json.put(Constant.FieldName.GROUPS, source.get(Constant.FieldName.GROUPNAME));
            json.put(Constant.FieldName.QUERYCONTEXT, source.get(Constant.FieldName.QUERYCONTEXT));
            //json.put(Constant.FieldName.CREATEDBY, source.get(Constant.FieldName.CREATEDBY));
            //json.put(Constant.FieldName.CREATEDON, source.get(Constant.FieldName.CREATEDON));
            //json.put(Constant.FieldName.LASTUPDATEDBY, source.get(Constant.FieldName.LASTUPDATEDBY));
            //json.put(Constant.FieldName.LASTUPDATEDON, source.get(Constant.FieldName.LASTUPDATEDON));
            return json;
        }else{
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }
    }

    public XContentBuilder update(String id, Json body) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.VIEWTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }else{

        }

        body.remove(Constant.FieldName._ID);

        //body.remove(Constant.FieldName.CREATEDBY);
        //body.remove(Constant.FieldName.CREATEDON);
        //body.put(Constant.FieldName.LASTUPDATEDBY, context.getUserName());
        //body.put(Constant.FieldName.LASTUPDATEDON, new Date());
        UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), Constant.FieldName.VIEWTYPENAME, id).setDoc(body).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", context.getIndex())
                .field("_type", Constant.FieldName.VIEWTYPENAME)
                .field("_id", id)
                .field("_version", updateResponse.getVersion())
                .field("_isCreated", updateResponse.isCreated())
                .endObject();
        return builder;
    }


    public XContentBuilder delete(String id) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.VIEWTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }else{

        }

        DeleteResponse deleteResponse = client.prepareDelete(context.getIndex(), Constant.FieldName.VIEWTYPENAME, id).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", context.getIndex())
                .field("_type", Constant.FieldName.VIEWTYPENAME)
                .field("_id", id)
                .field("_version", deleteResponse.getVersion())
                .field("found", deleteResponse.isFound())
                .endObject();
        return builder;
    }

    public List<Json> getViewsByUser(String id) throws IOException {
        List<Json> views = new ArrayList<Json>();
        //获取用户的所有view
        Client client = context.getClient();
        QueryBuilder qbUsers = QueryBuilders.termQuery(Constant.FieldName.USERS, id);
        SearchResponse srUsers = client.prepareSearch(context.getIndex()).setTypes(Constant.FieldName.VIEWTYPENAME).setQuery(qbUsers).execute().actionGet();
        for (SearchHit searchHitFields : srUsers.getHits()) {
            Json view = new Json();
            view.put(Constant.FieldName._ID, searchHitFields.getId());
            view.put(Constant.FieldName.VIEWNAME, searchHitFields.getSource().get(Constant.FieldName.VIEWNAME));
            view.put(Constant.FieldName.QUERYCONTEXT, searchHitFields.getSource().get(Constant.FieldName.QUERYCONTEXT));
            boolean exist = false;
            for(Json json:views){
                if (json.get(Constant.FieldName._ID)!=null){
                    exist = true;
                    break;
                }
            }
            if (!exist){
                views.add(view);
            }
        }

        //获取用户所在的所有组的所有view
        List<String> groups = userService.getGroupsOfUser(id);
        if(groups!=null && groups.size()>0){
            for(String groupId:groups){
                QueryBuilder quGroups = QueryBuilders.termQuery(Constant.FieldName.GROUPS, groupId);
                SearchResponse srGroups = client.prepareSearch(context.getIndex()).setTypes(Constant.FieldName.VIEWTYPENAME).setQuery(quGroups).execute().actionGet();
                for (SearchHit searchHitFields : srGroups.getHits()) {
                    Json view = new Json();
                    view.put(Constant.FieldName._ID, searchHitFields.getId());
                    view.put(Constant.FieldName.VIEWNAME, searchHitFields.getSource().get(Constant.FieldName.VIEWNAME));
                    view.put(Constant.FieldName.QUERYCONTEXT, searchHitFields.getSource().get(Constant.FieldName.QUERYCONTEXT));
                    boolean exist = false;
                    for(Json json:views){
                        if (json.get(Constant.FieldName._ID)!=null){
                            exist = true;
                            break;
                        }
                    }
                    if (!exist){
                        views.add(view);
                    }
                }
            }
        }
        return views;
    }

}

