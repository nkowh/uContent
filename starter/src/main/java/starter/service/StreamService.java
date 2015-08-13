package starter.service;


import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import starter.RequestContext;

import java.io.InputStream;
import java.util.List;

@Service
public class StreamService {

    @Autowired
    private RequestContext context;

    public XContentBuilder all(String type, String id) {
        return null;
    }

    public XContentBuilder update(String type, List<String> removedStreamIds, List<MultipartFile> files) {
        return null;
    }

    public XContentBuilder create(String type, String id, Integer order, List<MultipartFile> files) {
        return null;
    }

    public XContentBuilder get(String type, String id, String streamId) {
        return null;
    }

    public InputStream getStream(String type, String id, String streamId) {
        return null;
    }

    public XContentBuilder update(String type, String id, String streamId, MultipartFile files) {
        return null;
    }

    public XContentBuilder update(String type, String id, List<String> removedStreamIds, List<MultipartFile> files) {
        return null;
    }

    public XContentBuilder delete(String type, String id, String streamId) {
        return null;
    }


}
