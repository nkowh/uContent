//package starter.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.elasticsearch.action.get.GetResponse;
//import org.elasticsearch.action.update.UpdateResponse;
//import org.elasticsearch.common.xcontent.XContentBuilder;
//import org.elasticsearch.common.xcontent.json.JsonXContent;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import starter.RequestContext;
//import starter.rest.Json;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class AclService {
//
//    @Autowired
//    private RequestContext context;
//
//    @Autowired
//    private DocumentService documentService;
//
//    public XContentBuilder all(String type, String id) throws IOException {
//        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.read);
//        Map<String, Object> acl =  (Map<String, Object>)getResponse.getSource().get("_acl");
//        XContentBuilder builder = JsonXContent.contentBuilder();
//        builder.startObject();
//        Iterator<Map.Entry<String, Object>> it = acl.entrySet().iterator();
//        while(it.hasNext()){
//            Map.Entry<String, Object> entry = it.next();
//            builder.field(entry.getKey()).value(entry.getValue());
//        }
//        builder.endObject();
//        return builder;
//    }
//
//    public XContentBuilder update(String type, String id, Json body) throws IOException {
//        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.write);
//        documentService.processAcl(body, getResponse.getSource().get(Constant.FieldName.ACL));
//        Map<String, Object> acl = new HashMap<String, Object>();
//        acl.put(Constant.FieldName.ACL, getResponse.getSource().get(Constant.FieldName.ACL));
//        UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), type, id).setDoc(acl).execute().actionGet();
//        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
//        xContentBuilder.startObject()
//                .field("_index", context.getIndex())
//                .field("_type", type)
//                .field("_id", id)
//                .field("_version", updateResponse.getVersion())
//                .endObject();
//        return xContentBuilder;
//    }
//}
