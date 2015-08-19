package starter.service;

import com.fasterxml.jackson.core.JsonParser;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.compress.CompressedString;
import org.elasticsearch.common.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.rest.Json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TypeService {

    private static String indices="yuanyewen";

    @Autowired
    private RequestContext context;

    public XContentBuilder all() throws IOException {
        Client client = context.getClient();
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(indices).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        ImmutableOpenMap<String, MappingMetaData> types = mappings.get(indices);
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject();
        builder.startArray("documentTypes");
        for (ObjectObjectCursor<String, MappingMetaData> type : types) {
            String typeName = type.key;
            MappingMetaData mappingMetaData = types.get(typeName);
            try {
                if (mappingMetaData!=null){
                    String source = mappingMetaData.source().string();
                    Json parse = Json.parse(source);
                    LinkedHashMap<String, Object> typeObject = (LinkedHashMap<String, Object>)parse.get(typeName);
                    if (typeObject!=null){
                        LinkedHashMap<String, Object> typeInfo = (LinkedHashMap<String, Object>)typeObject.get("_meta");
                        if (typeInfo!=null){
                            boolean isDocType = (boolean)typeInfo.get("isDocType");
                            String displayName = String.valueOf(typeInfo.get("displayName"));
                            if ((displayName!=null)&&(isDocType)){
                                builder.startObject().field("name", typeName).field("displayName", displayName).endObject();
                            }
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
        builder.endArray();
        builder.endObject();
        System.out.println(builder.string());
        return builder;
    }

//    public XContentBuilder create(Json body) throws IOException {
//        return null;
//    }

    public XContentBuilder create(Json body) throws IOException {
        Client client = context.getClient();

        boolean acknowledged = false;
//
//        String jsonStr="{\n" +
//                "\t\"name\": \"testtype2\",\n" +
//                "\t\"displayName\": \"测试文档类型2\",\n" +
//                "\t\"description\": \"测试文档类型2\",\n" +
//                "\t\"properties\": [\n" +
//                "\t\t{\n" +
//                "\t\t\t\"name\":\"aaa\",\n" +
//                "\t\t\t\"type\":\"string\",\n" +
//                "\t\t\t\"isRequire\":true,\n" +
//                "\t\t\t\"defaultValue\":\"xx1\",\n" +
//                "\t\t\t\"partten\":\"\",\n" +
//                "\t\t\t\"promptMssage\":\"\",\n" +
//                "\t\t\t\"order\" : \"\"\n" +
//                "\t\t},{\n" +
//                "\t\t\t\"name\":\"bbb\",\n" +
//                "\t\t\t\"type\":\"float\",\n" +
//                "\t\t\t\"isRequire\":true,\n" +
//                "\t\t\t\"defaultValue\":\"1.45\",\n" +
//                "\t\t\t\"partten\":\"\",\n" +
//                "\t\t\t\"promptMssage\":\"\",\n" +
//                "\t\t\t\"order\" : \"\"\n" +
//                "\t\t},{\n" +
//                "\t\t\t\"name\":\"ccc\",\n" +
//                "\t\t\t\"type\":\"date\",\n" +
//                "\t\t\t\"isRequire\":true,\n" +
//                "\t\t\t\"defaultValue\":\"2015-05-20\",\n" +
//                "\t\t\t\"partten\":\"\",\n" +
//                "\t\t\t\"promptMssage\":\"\",\n" +
//                "\t\t\t\"order\" : \"\"\n" +
//                "\t\t},{\n" +
//                "\t\t\t\"name\":\"ddd\",\n" +
//                "\t\t\t\"type\":\"boolean\",\n" +
//                "\t\t\t\"isRequire\":true,\n" +
//                "\t\t\t\"defaultValue\":\"false\",\n" +
//                "\t\t\t\"partten\":\"\",\n" +
//                "\t\t\t\"promptMssage\":\"必须填ture或false\",\n" +
//                "\t\t\t\"order\" : \"\"\n" +
//                "\t\t}\n" +
//                "\t]\n" +
//                "}\n";
//
//        Json body = Json.parse(jsonStr);

        try {
            XContentBuilder builder= XContentFactory.jsonBuilder();
            String typeName="";
            if(body!=null){
                //获取typeName
                Object type = body.get("name");
                if ((type==null)||(((String)type).equals(""))){
                    //Exception
                }
                typeName = type.toString();

                Object description = body.get("description");

                //获取properties
                ArrayList<Object> properties = (ArrayList<Object>)body.get("properties");
                if ((properties==null)||properties.size()==0){
                    //Exception
                }

                builder.startObject();
                builder.startObject(typeName);

                //组装_meta
                builder.startObject("_meta")
                        .field("displayName", body.get("displayName").toString())
                        .field("description", body.get("description").toString())
                        .field("isDocType", true);
                //builder.startObject("properties");
                builder.startArray("properties");
                for(Object property:properties){
                    if (property!=null){
                        LinkedHashMap<String, Object> pro = (LinkedHashMap<String, Object>)property;
                        builder.startObject().field("name", pro.get("name").toString())
                                .field("type", pro.get("type").toString())
                                .field("isRequire", Boolean.valueOf(pro.get("isRequire").toString()))
                                .field("defaultValue", pro.get("defaultValue").toString())
                                .field("partten", pro.get("partten").toString())
                                .field("promptMssage", pro.get("promptMssage").toString())
                                .field("order", pro.get("order").toString())
                                .endObject();
                    }
                }
                builder.endArray();
                //builder.endObject();  //end of properties
                builder.endObject(); //end of _meta


                //组装properties
                builder.startObject("properties")
                        .startObject("name").field("type", "string").field("store", "yes").endObject()
                        .startObject("description").field("type", "string").field("store", "yes").endObject();

                //组装stream属性
                builder.startObject("streams").startObject("properties")
                        .startObject("streamId").field("type", "string").field("store", "yes").endObject()
                        .startObject("streamName").field("type", "string").field("store", "yes").endObject()
                        .startObject("size").field("type", "long").field("store", "yes").endObject()
                        .startObject("format").field("type", "string").field("store", "yes").endObject()
                        .startObject("encoding").field("type", "string").field("store", "yes").endObject()
                        .endObject()
                        .endObject();

                //组装创建信息属性
                builder.startObject("createBy").field("type", "string").field("store", "yes").endObject()
                        .startObject("creationDate").field("type", "date").field("store", "yes").endObject()
                        .startObject("lastModifiedBy").field("type", "string").field("store", "yes").endObject()
                        .startObject("lastModificationDate").field("type", "date").field("store", "yes").endObject();

                //组装自定义属性
                for(Object property:properties){
                    if (property!=null){
                        LinkedHashMap<String, Object> pro = (LinkedHashMap<String, Object>)property;
                        builder.startObject(pro.get("name").toString())
                                .field("type", pro.get("type").toString())
                                .field("isRequire", Boolean.valueOf(pro.get("isRequire").toString()))
                                .field("defaultValue", pro.get("defaultValue").toString())
                                .field("partten", pro.get("partten").toString())
                                .field("promptMssage", pro.get("promptMssage").toString())
                                .field("order", pro.get("order").toString())
                                .endObject();
                    }
                }

                builder.endObject();//end of properties

                builder.endObject();//end of typeName
                builder.endObject();
            }

            System.out.println(builder.string());

            //创建mapping
            PutMappingRequest mapping = Requests.putMappingRequest(indices).type(typeName).source(builder);
            PutMappingResponse putMappingResponse = client.admin().indices().putMapping(mapping).actionGet();
            acknowledged = putMappingResponse.isAcknowledged();
            //System.out.println(builder.string());
            //client.close();

            putMappingResponse.isAcknowledged();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //返回相应
        return XContentFactory.jsonBuilder().startObject().field("acknowledged",acknowledged).endObject();
    }

    public XContentBuilder get(String id) throws IOException {
        Client client = context.getClient();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(indices).
                addTypes(id).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        MappingMetaData mappingMetaData = mappings.get(indices).get(id);
        if (mappingMetaData!=null){
            String source = mappingMetaData.source().string();
            Json parse = Json.parse(source);
            LinkedHashMap<String, Object> type = (LinkedHashMap<String, Object>)parse.get(id);
            LinkedHashMap<String, Object> meta = (LinkedHashMap<String, Object>)type.get("_meta");

            builder.startObject();
            //builder.field("","").
            ArrayList<Object> properties = (ArrayList<Object>)meta.get("properties");
            if ((properties==null)||properties.size()==0){
                //Exception
            }else{
                builder.startArray("properties");
                for(Object property:properties){
                    if (property!=null){
                        LinkedHashMap<String, Object> pro = (LinkedHashMap<String, Object>)property;
                        builder.startObject().field("name", pro.get("name").toString())
                                .field("type", pro.get("type").toString())
                                .field("isRequire", Boolean.valueOf(pro.get("isRequire").toString()))
                                .field("defaultValue", pro.get("defaultValue").toString())
                                .field("partten", pro.get("partten").toString())
                                .field("promptMssage", pro.get("promptMssage").toString())
                                .field("order", pro.get("order").toString())
                                .endObject();
                    }
                }
                builder.endArray();//end of properties

            }
            builder.endObject(); //end of _meta
        }
        System.out.println(builder.string());
        return builder;
    }

    public XContentBuilder update(String id) {


        return null;
    }

    public XContentBuilder delete(String id) throws IOException {
        Client client = context.getClient();
        boolean acknowledged = false;
        DeleteMappingRequest mapping = Requests.deleteMappingRequest(indices).types(id);
        DeleteMappingResponse deleteMappingResponse = client.admin().indices().deleteMapping(mapping).actionGet();
        acknowledged = deleteMappingResponse.isAcknowledged();
        return XContentFactory.jsonBuilder().startObject().field("acknowledged",acknowledged).endObject();
    }
}
