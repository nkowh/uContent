package starter.service;


import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.rest.Json;

import java.util.List;

@Service
public class GroupService {

    @Autowired
    private RequestContext context;

    public XContentBuilder all() {
        return null;
    }

    public XContentBuilder create(Json body) {
        return null;
    }

    public XContentBuilder get(String id) {
        return null;
    }

    public XContentBuilder getUsers(String id) {
        return null;
    }

    public XContentBuilder update(String id) {
        return null;
    }

    public XContentBuilder delete(String id) {
        return null;
    }

    public XContentBuilder refUsers(List<String> userIds) {
        return null;
    }
}
