package starter.service;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.rest.Json;
import starter.uContentException;

import java.util.List;
import java.util.Map;

@Service
public class AclService {

    @Autowired
    private RequestContext context;

    @Autowired
    private DocumentService documentService;

    public List<Map<String, Object>> all(String type, String id) {
        GetResponse getResponse = context.getClient().prepareGet(context.getIndex(), type, id).execute().actionGet();
        if (!getResponse.isExists()) {
            throw new uContentException("Not found", HttpStatus.NOT_FOUND);
        }
        if(!documentService.hasPermission(context.getUserName(), getResponse.getSource().get("_acl"), Constant.Permission.READ)){
            throw new uContentException("Forbidden", HttpStatus.FORBIDDEN);
        }
        return (List<Map<String, Object>>)getResponse.getSource().get("_acl");
    }

    public Json update(String type, String id, Json body) {
        return null;
    }
}
