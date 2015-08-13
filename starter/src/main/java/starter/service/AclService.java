package starter.service;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.rest.Json;

@Service
public class AclService {

    @Autowired
    private RequestContext context;

    public XContentBuilder all(String type, String id) {
        return null;
    }

    public XContentBuilder update(String type, String id, Json body) {
        return null;
    }
}
