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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.rest.Json;
import starter.uContentException;

import java.io.IOException;
import java.util.*;

@Service
public class TypeService {

    @Autowired
    private RequestContext context;

    public XContentBuilder all() throws IOException {
        Client client = context.getClient();
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(context.getIndex()).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        ImmutableOpenMap<String, MappingMetaData> types = mappings.get(context.getIndex());
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
                            boolean isDocType = (boolean)typeInfo.get(Constant.FieldName.ISDOCTYPE);
                            String displayName = String.valueOf(typeInfo.get(Constant.FieldName.DISPLAYNAME));
                            String description = String.valueOf(typeInfo.get(Constant.FieldName.DESCRIPTION));
                            if (isDocType){
                                builder.startObject().field(Constant.FieldName.NAME, typeName)
                                        .field(Constant.FieldName.DISPLAYNAME, displayName)
                                        .field(Constant.FieldName.DESCRIPTION, description)
                                        .endObject();
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

    public XContentBuilder create(Json body) throws IOException {
        Client client = context.getClient();

        boolean acknowledged = false;

        try {
            XContentBuilder builder= XContentFactory.jsonBuilder();
            String typeName="";
            if(body!=null){
                //获取typeName
                Object type = body.get(Constant.FieldName.NAME);
                if ((type==null)||(((String)type).equals(""))){
                    //Exception
                }
                typeName = type.toString();

                Object description = body.get(Constant.FieldName.DESCRIPTION);

                //获取properties
                ArrayList<Object> properties = (ArrayList<Object>)body.get(Constant.FieldName.PROPERTIES);
                if ((properties==null)||properties.size()==0){
                    //Exception
                }

                builder.startObject();
                builder.startObject(typeName);

                //组装_meta
                builder.startObject("_meta")
                        .field(Constant.FieldName.DISPLAYNAME, body.get(Constant.FieldName.DISPLAYNAME).toString())
                        .field(Constant.FieldName.DESCRIPTION, body.get(Constant.FieldName.DESCRIPTION).toString())
                        .field(Constant.FieldName.ISDOCTYPE, true);
                //builder.startObject(Constant.FieldName.PROPERTIES);
                builder.startArray(Constant.FieldName.PROPERTIES);
                for(Object property:properties){
                    if (property!=null){
                        LinkedHashMap<String, Object> pro = (LinkedHashMap<String, Object>)property;
                        builder.startObject().field(Constant.FieldName.NAME, pro.get(Constant.FieldName.NAME).toString())
                                .field(Constant.FieldName.TYPE, pro.get(Constant.FieldName.TYPE).toString())
                                .field(Constant.FieldName.REQUIRED, Boolean.valueOf(pro.get(Constant.FieldName.REQUIRED).toString()))
                                .field(Constant.FieldName.DEFAULTVALUE, pro.get(Constant.FieldName.DEFAULTVALUE).toString())
                                .field(Constant.FieldName.PATTERN, pro.get(Constant.FieldName.PATTERN).toString())
                                .field(Constant.FieldName.PROMPTMESSAGE, pro.get(Constant.FieldName.PROMPTMESSAGE).toString())
                                .field(Constant.FieldName.ORDER, pro.get(Constant.FieldName.ORDER).toString())
                                .endObject();
                    }
                }
                builder.endArray();
                //builder.endObject();  //end of properties
                builder.endObject(); //end of _meta


                //组装properties
                builder.startObject(Constant.FieldName.PROPERTIES)
                        .startObject(Constant.FieldName.NAME).field(Constant.FieldName.TYPE, "string").field("store", "yes").endObject()
                        .startObject(Constant.FieldName.DESCRIPTION).field(Constant.FieldName.TYPE, "string").field("store", "yes").endObject();

                //组装stream属性
                builder.startObject(Constant.FieldName.STREAMS).startObject(Constant.FieldName.PROPERTIES)
                        .startObject(Constant.FieldName.STREAMID).field(Constant.FieldName.TYPE, "string").field("store", "yes").endObject()
                        .startObject(Constant.FieldName.STREAMNAME).field(Constant.FieldName.TYPE, "string").field("store", "yes").endObject()
                        .startObject(Constant.FieldName.LENGTH).field(Constant.FieldName.TYPE, "long").field("store", "yes").endObject()
                        .startObject(Constant.FieldName.CONTENTTYPE).field(Constant.FieldName.TYPE, "string").field("store", "yes").endObject()
                        .endObject()
                        .endObject();

                //组装_acl属性
                builder.startObject("_acl").field(Constant.FieldName.TYPE, "nested").endObject();

                //组装创建信息属性
                builder.startObject(Constant.FieldName.CREATEDBY).field(Constant.FieldName.TYPE, "string").field("store", "yes").endObject()
                        .startObject(Constant.FieldName.CREATEDON).field(Constant.FieldName.TYPE, "date").field("store", "yes").endObject()
                        .startObject(Constant.FieldName.LASTUPDATEDBY).field(Constant.FieldName.TYPE, "string").field("store", "yes").endObject()
                        .startObject(Constant.FieldName.LASTUPDATEDON).field(Constant.FieldName.TYPE, "date").field("store", "yes").endObject();

                //组装自定义属性
                for(Object property:properties){
                    if (property!=null){
                        LinkedHashMap<String, Object> pro = (LinkedHashMap<String, Object>)property;
                        builder.startObject(pro.get(Constant.FieldName.NAME).toString())
                                .field(Constant.FieldName.TYPE, pro.get(Constant.FieldName.TYPE).toString())
                                .field(Constant.FieldName.REQUIRED, Boolean.valueOf(pro.get(Constant.FieldName.REQUIRED).toString()))
                                .field(Constant.FieldName.DEFAULTVALUE, pro.get(Constant.FieldName.DEFAULTVALUE).toString())
                                .field(Constant.FieldName.PATTERN, pro.get(Constant.FieldName.PATTERN).toString())
                                .field(Constant.FieldName.PROMPTMESSAGE, pro.get(Constant.FieldName.PROMPTMESSAGE).toString())
                                .field(Constant.FieldName.ORDER, pro.get(Constant.FieldName.ORDER).toString())
                                .endObject();
                    }
                }

                builder.endObject();//end of properties

                builder.endObject();//end of typeName
                builder.endObject();
            }

            System.out.println(builder.string());

            //创建mapping
            PutMappingRequest mapping = Requests.putMappingRequest(context.getIndex()).type(typeName).source(builder);
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
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(context.getIndex()).
                addTypes(id).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        MappingMetaData mappingMetaData = mappings.get(context.getIndex()).get(id);
        if (mappingMetaData!=null){
            String source = mappingMetaData.source().string();
            Json parse = Json.parse(source);
            LinkedHashMap<String, Object> type = (LinkedHashMap<String, Object>)parse.get(id);
            LinkedHashMap<String, Object> meta = (LinkedHashMap<String, Object>)type.get("_meta");

            builder.startObject();
            //builder.field("","").
            ArrayList<Object> properties = (ArrayList<Object>)meta.get(Constant.FieldName.PROPERTIES);
            if ((properties==null)||properties.size()==0){
                //Exception
            }else{
                builder.startArray(Constant.FieldName.PROPERTIES);
                for(Object property:properties){
                    if (property!=null){
                        LinkedHashMap<String, Object> pro = (LinkedHashMap<String, Object>)property;
                        builder.startObject().field(Constant.FieldName.NAME, pro.get(Constant.FieldName.NAME).toString())
                                .field(Constant.FieldName.TYPE, pro.get(Constant.FieldName.TYPE).toString())
                                .field(Constant.FieldName.REQUIRED, Boolean.valueOf(pro.get(Constant.FieldName.REQUIRED).toString()))
                                .field(Constant.FieldName.DEFAULTVALUE, pro.get(Constant.FieldName.DEFAULTVALUE).toString())
                                .field(Constant.FieldName.PATTERN, pro.get(Constant.FieldName.PATTERN).toString())
                                .field(Constant.FieldName.PROMPTMESSAGE, pro.get(Constant.FieldName.PROMPTMESSAGE).toString())
                                .field(Constant.FieldName.ORDER, pro.get(Constant.FieldName.ORDER).toString())
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


    public Map<String, Map<String, Object>> getProperties(String id) throws IOException {
        Client client = context.getClient();
        Map<String, Map<String, Object>> pros = new HashMap<String, Map<String, Object>>();
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(context.getIndex()).
                addTypes(id).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if (mappings==null||mappings.size()==0){
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }

        //填装基本属性
        pros.put(Constant.FieldName.NAME,transforProperty(Constant.FieldName.NAME,"string",true,"","","",""));
        pros.put(Constant.FieldName.DESCRIPTION,transforProperty(Constant.FieldName.DESCRIPTION,"string",true,"","","",""));
        pros.put(Constant.FieldName.CREATEDBY,transforProperty(Constant.FieldName.CREATEDBY,"string",true,"","","",""));
        pros.put(Constant.FieldName.CREATEDON,transforProperty(Constant.FieldName.CREATEDON,"date",true,"","","",""));
        pros.put(Constant.FieldName.LASTUPDATEDBY,transforProperty(Constant.FieldName.LASTUPDATEDBY,"string",true,"","","",""));
        pros.put(Constant.FieldName.LASTUPDATEDON,transforProperty(Constant.FieldName.LASTUPDATEDON,"date",true,"","","",""));

        MappingMetaData mappingMetaData = mappings.get(context.getIndex()).get(id);
        if (mappingMetaData!=null){
            String source = mappingMetaData.source().string();
            Json parse = Json.parse(source);
            LinkedHashMap<String, Object> type = (LinkedHashMap<String, Object>)parse.get(id);
            LinkedHashMap<String, Object> meta = (LinkedHashMap<String, Object>)type.get("_meta");


            ArrayList<Object> properties = (ArrayList<Object>)meta.get(Constant.FieldName.PROPERTIES);
            if ((properties==null)||properties.size()==0){
                //Exception
            }else{
                for(Object property:properties){
                    if (property!=null){
                        LinkedHashMap<String, Object> pro = (LinkedHashMap<String, Object>)property;
                        pros.put(pro.get(Constant.FieldName.NAME).toString(),transforProperty(
                                pro.get(Constant.FieldName.NAME).toString(),
                                pro.get(Constant.FieldName.TYPE).toString(),
                                Boolean.valueOf(pro.get(Constant.FieldName.REQUIRED).toString()),
                                pro.get(Constant.FieldName.DEFAULTVALUE).toString(),
                                pro.get(Constant.FieldName.PATTERN).toString(),
                                pro.get(Constant.FieldName.PROMPTMESSAGE).toString(),
                                pro.get(Constant.FieldName.ORDER).toString()));
                    }
                }
            }
        }
        return pros;
    }

    public XContentBuilder update(String id, Json body) throws IOException {
        Client client = context.getClient();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(context.getIndex()).
                addTypes(id).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if (mappings==null||mappings.size()==0){
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }

        Map<String, Map<String, Object>> properties = getProperties(id);

        return create(body);
    }

    public XContentBuilder delete(String id) throws IOException {
        Client client = context.getClient();
        boolean acknowledged = false;
        DeleteMappingRequest mapping = Requests.deleteMappingRequest(context.getIndex()).types(id);
        DeleteMappingResponse deleteMappingResponse = client.admin().indices().deleteMapping(mapping).actionGet();
        acknowledged = deleteMappingResponse.isAcknowledged();
        return XContentFactory.jsonBuilder().startObject().field("acknowledged",acknowledged).endObject();
    }

    private Map<String, Object> transforProperty(String name, String type, boolean required,
                                                String defaultValue, String pattern,
                                                String promptMessage, String order){
        Map<String, Object> property = new HashMap<String, Object>();
        property.put(Constant.FieldName.NAME, name);
        property.put(Constant.FieldName.TYPE, type);
        property.put(Constant.FieldName.REQUIRED, required);
        property.put(Constant.FieldName.DEFAULTVALUE, defaultValue);
        property.put(Constant.FieldName.PATTERN, pattern);
        property.put(Constant.FieldName.PROMPTMESSAGE, promptMessage);
        property.put(Constant.FieldName.ORDER, order);
        return property;
    }
}
