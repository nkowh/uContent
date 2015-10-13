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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TagService {

    @Autowired
    private RequestContext context;

    public void initialTagData() throws IOException {
        Client client = context.getClient();

        //创建View Mapping
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(context.getIndex()).addTypes(Constant.FieldName.TAGTYPENAME).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if(mappings.size()==0){
            //冇得，那就搞一个吧。。。
            XContentBuilder builder= XContentFactory.jsonBuilder();
            builder.startObject();
            builder.startObject(Constant.FieldName.TAGTYPENAME);
            builder.startObject("properties")
                    .startObject(Constant.FieldName.TAGCONTEXT).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    .startObject(Constant.FieldName.DESCRIPTION).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject();
                    //.startObject(Constant.FieldName.CREATEDBY).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    //.startObject(Constant.FieldName.CREATEDON).field("type", "date").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    //.startObject(Constant.FieldName.LASTUPDATEDBY).field("type", "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                    //.startObject(Constant.FieldName.LASTUPDATEDON).field("type", "date").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject();
            builder.endObject();//end of typeName
            builder.endObject();
            //创建mapping
            PutMappingRequest mapping = Requests.putMappingRequest(context.getIndex()).type(Constant.FieldName.TAGTYPENAME).source(builder);
            PutMappingResponse putMappingResponse = client.admin().indices().putMapping(mapping).actionGet();
        }else{
            //艹，居然有！！！！！
        }
    }

    public List<Json> all() throws IOException {
        Client client = context.getClient();
        SearchResponse searchResponse = client.prepareSearch(context.getIndex()).setTypes(Constant.FieldName.TAGTYPENAME).execute().actionGet();
        SearchHits hits = searchResponse.getHits();
        List<Json> tags = new ArrayList<Json>();
//        XContentBuilder builder= XContentFactory.jsonBuilder();
//        builder.startObject().field("total", searchResponse.getHits().totalHits());
//        builder.startArray("views");
        for (SearchHit searchHitFields : searchResponse.getHits()) {
            Json view = new Json();
            view.put(Constant.FieldName._ID, searchHitFields.getId());
            view.put(Constant.FieldName.TAGCONTEXT, searchHitFields.getSource().get(Constant.FieldName.TAGCONTEXT));
            view.put(Constant.FieldName.DESCRIPTION, searchHitFields.getSource().get(Constant.FieldName.DESCRIPTION));
            tags.add(view);
        }
//        builder.endArray();
//        builder.endObject();
        return tags;
    }

    public XContentBuilder create(Json body) throws IOException {
        Client client = context.getClient();
        XContentBuilder builder= XContentFactory.jsonBuilder();

        body.remove(Constant.FieldName._ID);
        //body.put(Constant.FieldName.CREATEDBY, context.getUserName());
        //body.put(Constant.FieldName.CREATEDON, new Date());
        //body.put(Constant.FieldName.LASTUPDATEDBY, null);
        //body.put(Constant.FieldName.LASTUPDATEDON, null);
        IndexResponse indexResponse = client.prepareIndex(context.getIndex(), Constant.FieldName.TAGTYPENAME).setSource(body).execute().actionGet();
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
        GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.TAGTYPENAME, id).execute().actionGet();
        Map<String, Object> source = getResponse.getSource();
        if (getResponse.isExists()){
            Json json = new Json();
            json.put(Constant.FieldName._ID, id);
            json.put(Constant.FieldName.TAGCONTEXT, source.get(Constant.FieldName.TAGCONTEXT));
            json.put(Constant.FieldName.DESCRIPTION, source.get(Constant.FieldName.DESCRIPTION));
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
        GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.TAGTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }else{

        }

        body.remove(Constant.FieldName._ID);

        //body.remove(Constant.FieldName.CREATEDBY);
        //body.remove(Constant.FieldName.CREATEDON);
        //body.put(Constant.FieldName.LASTUPDATEDBY, context.getUserName());
        //body.put(Constant.FieldName.LASTUPDATEDON, new Date());
        UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), Constant.FieldName.TAGTYPENAME, id).setDoc(body).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", context.getIndex())
                .field("_type", Constant.FieldName.TAGTYPENAME)
                .field("_id", id)
                .field("_version", updateResponse.getVersion())
                .field("_isCreated", updateResponse.isCreated())
                .endObject();
        return builder;
    }


    public XContentBuilder delete(String id) throws IOException {
        Client client = context.getClient();
        GetResponse getResponse = client.prepareGet(context.getIndex(), Constant.FieldName.TAGTYPENAME, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }else{

        }

        DeleteResponse deleteResponse = client.prepareDelete(context.getIndex(), Constant.FieldName.TAGTYPENAME, id).execute().actionGet();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject()
                .field("_index", context.getIndex())
                .field("_type", Constant.FieldName.TAGTYPENAME)
                .field("_id", id)
                .field("_version", deleteResponse.getVersion())
                .field("found", deleteResponse.isFound())
                .endObject();
        return builder;
    }

}

