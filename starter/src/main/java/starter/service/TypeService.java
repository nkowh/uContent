package starter.service;

import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
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
        return builder;
    }

    public List<String> getAllTypes() throws IOException {
        List<String> allTypes = new ArrayList<String>();
        XContentBuilder all = all();
        Json parse = Json.parse(all.string());
        Object documentTypes = parse.get("documentTypes");
        if(documentTypes!=null && documentTypes instanceof List){
            List<HashMap<String, Object>> types = (ArrayList<HashMap<String, Object>>)documentTypes;
            for(HashMap<String, Object> type:types){
                allTypes.add(type.get(Constant.FieldName.NAME).toString());
            }
        }
        return allTypes;
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
                                .field(Constant.FieldName.INDEX, pro.get(Constant.FieldName.INDEX).toString())
                                .field(Constant.FieldName.INDEXANALYZER, pro.get(Constant.FieldName.INDEXANALYZER).toString())
                                .field(Constant.FieldName.SEARCHANALYZER, pro.get(Constant.FieldName.SEARCHANALYZER).toString())
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
                        .startObject(Constant.FieldName.NAME).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                        .startObject(Constant.FieldName.DESCRIPTION).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject();

                //组装stream属性
                builder.startObject(Constant.FieldName.STREAMS).startObject(Constant.FieldName.PROPERTIES)
                        .startObject(Constant.FieldName.STREAMID).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                        .startObject(Constant.FieldName.STREAMNAME).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                        .startObject(Constant.FieldName.LENGTH).field(Constant.FieldName.TYPE, "long").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                        .startObject(Constant.FieldName.CONTENTTYPE).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                        .startObject(Constant.FieldName.FULLTEXT).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.ANALYZED).
                                    field(Constant.FieldName.INDEXANALYZER, Constant.DEFAULT_INDEXANALYZER).
                                    field(Constant.FieldName.SEARCHANALYZER, Constant.DEFAULT_SEARCHANALYZER).endObject()
                        .endObject()
                        .endObject();

                //组装_acl属性
//                builder.startObject("_acl").field(Constant.FieldName.TYPE, "nested")
//                        .startObject(Constant.FieldName.PROPERTIES)
//                        .startObject(Constant.FieldName.PERMISSION).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
//                        .startObject(Constant.FieldName.USER).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
//                        .startObject(Constant.FieldName.GROUP).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
//                        .endObject()
//                        .endObject();

                builder.startObject("_acl")
                        .startObject(Constant.FieldName.PROPERTIES)
                            .startObject(Constant.FieldName.READ)
                                .startObject(Constant.FieldName.PROPERTIES)
                                    .startObject(Constant.FieldName.USERS).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                                    .startObject(Constant.FieldName.GROUPS).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                                .endObject()
                            .endObject()
                            .startObject(Constant.FieldName.WRITE)
                                .startObject(Constant.FieldName.PROPERTIES)
                                    .startObject(Constant.FieldName.USERS).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                                    .startObject(Constant.FieldName.GROUPS).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                                .endObject()
                            .endObject()
                        .endObject()
                        .endObject();


                //组装创建信息属性
                builder.startObject(Constant.FieldName.CREATEDBY).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                        .startObject(Constant.FieldName.CREATEDON).field(Constant.FieldName.TYPE, "date").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                        .startObject(Constant.FieldName.LASTUPDATEDBY).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject()
                        .startObject(Constant.FieldName.LASTUPDATEDON).field(Constant.FieldName.TYPE, "date").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject();

                //组装Tag属性
                builder.startObject(Constant.FieldName.TAG).field(Constant.FieldName.TYPE, "string").field(Constant.FieldName.INDEX, Constant.FieldName.NOT_ANALYZED).endObject();

                //组装自定义属性
                for(Object property:properties){
                    if (property!=null){
                        LinkedHashMap<String, Object> pro = (LinkedHashMap<String, Object>)property;
                        builder.startObject(pro.get(Constant.FieldName.NAME).toString());
                        builder.field(Constant.FieldName.TYPE, pro.get(Constant.FieldName.TYPE).toString());
                        //非bool类型的字段才做是否分词控制
                        if(!pro.get(Constant.FieldName.TYPE).toString().toUpperCase().equals("BOOLEAN")){
                            builder.field(Constant.FieldName.INDEX, pro.get(Constant.FieldName.INDEX).toString());
                            if ((!StringUtils.isEmpty(pro.get(Constant.FieldName.INDEX)))&&(pro.get(Constant.FieldName.INDEX).equals(Constant.FieldName.ANALYZED))){
                                Object indexAnalyzer = pro.get(Constant.FieldName.INDEXANALYZER);
                                Object searchAnalyzer = pro.get(Constant.FieldName.SEARCHANALYZER);
                                if (StringUtils.isEmpty(indexAnalyzer) && StringUtils.isEmpty(searchAnalyzer)){

                                }else if ((!StringUtils.isEmpty(indexAnalyzer)) && StringUtils.isEmpty(searchAnalyzer)){
                                    builder.field(Constant.FieldName.INDEXANALYZER, pro.get(Constant.FieldName.INDEXANALYZER).toString())
                                            .field(Constant.FieldName.SEARCHANALYZER, pro.get(Constant.FieldName.INDEXANALYZER).toString());
                                }else if (StringUtils.isEmpty(indexAnalyzer) && (!StringUtils.isEmpty(searchAnalyzer))){
                                    builder.field(Constant.FieldName.INDEXANALYZER, pro.get(Constant.FieldName.SEARCHANALYZER).toString())
                                            .field(Constant.FieldName.SEARCHANALYZER, pro.get(Constant.FieldName.SEARCHANALYZER).toString());
                                }else if ((!StringUtils.isEmpty(indexAnalyzer)) && (!StringUtils.isEmpty(searchAnalyzer))){
                                    builder.field(Constant.FieldName.INDEXANALYZER, pro.get(Constant.FieldName.INDEXANALYZER).toString())
                                            .field(Constant.FieldName.SEARCHANALYZER, pro.get(Constant.FieldName.SEARCHANALYZER).toString());
                                }
                            }

                        }
                        builder.endObject();
                    }
                }

                builder.endObject();//end of properties

                builder.endObject();//end of typeName
                builder.endObject();
            }

            //创建mapping
            PutMappingRequest mapping = Requests.putMappingRequest(context.getIndex()).type(typeName).source(builder);
            PutMappingResponse putMappingResponse = client.admin().indices().putMapping(mapping).actionGet();
            acknowledged = putMappingResponse.isAcknowledged();
            //System.out.println(builder.string());
            //client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //返回相应
        return XContentFactory.jsonBuilder().startObject().field("acknowledged",acknowledged).endObject();
    }

    public XContentBuilder get(String id) throws IOException {

        List<String> allTypes = getAllTypes();

        Client client = context.getClient();
        XContentBuilder builder= XContentFactory.jsonBuilder();
        GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings().addIndices(context.getIndex()).
                addTypes(id).get();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getMappingsResponse.getMappings();
        if (mappings==null||mappings.size()==0){
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }
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
                                .field(Constant.FieldName.INDEX, pro.get(Constant.FieldName.INDEX).toString())
                                .field(Constant.FieldName.INDEXANALYZER, pro.get(Constant.FieldName.INDEXANALYZER).toString())
                                .field(Constant.FieldName.SEARCHANALYZER, pro.get(Constant.FieldName.SEARCHANALYZER).toString())
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
        pros.put(Constant.FieldName.NAME,makeProperty(Constant.FieldName.NAME, "string", Constant.FieldName.NOT_ANALYZED, "", "", true, "", "", "", ""));
        pros.put(Constant.FieldName.DESCRIPTION,makeProperty(Constant.FieldName.DESCRIPTION, "string", Constant.FieldName.NOT_ANALYZED, "", "", false, "", "", "", ""));
        pros.put(Constant.FieldName.CREATEDBY,makeProperty(Constant.FieldName.CREATEDBY, "string", Constant.FieldName.NOT_ANALYZED, "", "", false, "", "", "", ""));
        pros.put(Constant.FieldName.CREATEDON,makeProperty(Constant.FieldName.CREATEDON, "date", Constant.FieldName.NOT_ANALYZED, "", "", false, "", "", "", ""));
        pros.put(Constant.FieldName.LASTUPDATEDBY,makeProperty(Constant.FieldName.LASTUPDATEDBY, "string", Constant.FieldName.NOT_ANALYZED, "", "", false, "", "", "", ""));
        pros.put(Constant.FieldName.LASTUPDATEDON,makeProperty(Constant.FieldName.LASTUPDATEDON, "date", Constant.FieldName.NOT_ANALYZED, "", "", false, "", "", "", ""));

        pros.put(Constant.FieldName.TAG,makeProperty(Constant.FieldName.TAG, "string", Constant.FieldName.NOT_ANALYZED, "", "", false, "", "", "", ""));

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
                        pros.put(pro.get(Constant.FieldName.NAME).toString(),makeProperty(
                                pro.get(Constant.FieldName.NAME).toString(),
                                pro.get(Constant.FieldName.TYPE).toString(),
                                pro.get(Constant.FieldName.INDEX).toString(),
                                pro.get(Constant.FieldName.INDEXANALYZER).toString(),
                                pro.get(Constant.FieldName.SEARCHANALYZER).toString(),
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

    private Map<String, Object> makeProperty(String name, String type,
                                                String index, String indexAnalyzer, String searchAnalyzer,
                                                boolean required,
                                                String defaultValue, String pattern,
                                                String promptMessage, String order){
        Map<String, Object> property = new HashMap<String, Object>();
        property.put(Constant.FieldName.NAME, name);
        property.put(Constant.FieldName.TYPE, type);
        property.put(Constant.FieldName.INDEX, index);
        property.put(Constant.FieldName.INDEXANALYZER, indexAnalyzer);
        property.put(Constant.FieldName.SEARCHANALYZER, searchAnalyzer);
        property.put(Constant.FieldName.REQUIRED, required);
        property.put(Constant.FieldName.DEFAULTVALUE, defaultValue);
        property.put(Constant.FieldName.PATTERN, pattern);
        property.put(Constant.FieldName.PROMPTMESSAGE, promptMessage);
        property.put(Constant.FieldName.ORDER, order);
        return property;
    }

}
