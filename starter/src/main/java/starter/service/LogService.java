package starter.service;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import starter.RequestContext;
import starter.rest.Json;

@Service
public class LogService {
    @Autowired
    private RequestContext context;

    public XContentBuilder query(String type, Json parse, int start, int limit, String sort) {
        return null;
    }
}
