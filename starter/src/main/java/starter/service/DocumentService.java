package starter.service;


import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import starter.RequestContext;
import starter.rest.Json;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private RequestContext context;

    public XContentBuilder query(String type, Json parse, int start, int limit, String sort) throws IOException {
        XContentBuilder builder = JsonXContent.contentBuilder();
        builder.startObject().field("aaa","a1").field("bbb","b1").endObject();
        return builder;
    }

    public XContentBuilder create(String type, Json body) {
        return null;
    }

    public XContentBuilder create(String type, Json body, List<MultipartFile> files) {
        return null;
    }

    public XContentBuilder head(String type, String id) {
        return null;
    }

    public XContentBuilder get(String type, String id) {
        return null;
    }

    public XContentBuilder update(String type, String id, Json body) {
        return null;
    }

    public XContentBuilder patch(String type, String id, Json body) {
        return null;
    }

    public XContentBuilder delete(String type, String id) {
        return null;
    }
}
