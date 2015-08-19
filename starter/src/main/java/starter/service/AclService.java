package starter.service;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.hibernate.validator.constraints.br.CNPJ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.rest.Json;
import starter.uContentException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class AclService {

    @Autowired
    private RequestContext context;

    @Autowired
    private DocumentService documentService;

    public XContentBuilder all(String type, String id) throws IOException {
        GetResponse getResponse = context.getClient().prepareGet(context.getIndex(), type, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }
        if(!documentService.hasPermission(context.getUserName(), getResponse.getSource().get("_acl"), Constant.Permission.READ)){
            throw new uContentException("Forbidden", HttpStatus.FORBIDDEN);
        }
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
        xContentBuilder.startArray();
        List<Map<String, Object>> acl =  (List<Map<String, Object>>)getResponse.getSource().get("_acl");
        for(Map<String, Object> map : acl){
            xContentBuilder.startObject();
            Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String, Object> entry = it.next();
                xContentBuilder.field(entry.getKey(), entry.getValue());
            }
            xContentBuilder.endObject();
        }
        xContentBuilder.endArray();
        return xContentBuilder;
    }

    public XContentBuilder update(String type, String id, Json body) throws IOException {
        GetResponse getResponse = context.getClient().prepareGet(context.getIndex(), type, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }
        if (!documentService.hasPermission(context.getUserName(), getResponse.getSource().get("_acl"), Constant.Permission.UPDATE)) {
            throw new uContentException("Forbidden", HttpStatus.FORBIDDEN);
        }
        documentService.processAcl(body, getResponse.getSource().get("_acl"));
        Map<String, Object> acl = new HashMap<String, Object>();
        acl.put("_acl", getResponse.getSource().get("_acl"));
        UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), type, id).setDoc(acl).execute().actionGet();
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
        xContentBuilder.startObject()
                .field("_index", context.getIndex())
                .field("_type", type)
                .field("_id", id)
                .field("_version", updateResponse.getVersion())
                .field("_isCreated", updateResponse.isCreated())
                .endObject();
        return xContentBuilder;
    }
}
