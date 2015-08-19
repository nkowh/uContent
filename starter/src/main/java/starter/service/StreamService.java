package starter.service;


import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import starter.RequestContext;
import starter.service.fs.FileSystem;
import starter.uContentException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class StreamService {

    @Autowired
    private RequestContext context;

    @Autowired
    private FileSystem fs;

    @Autowired
    private DocumentService documentService;

    public XContentBuilder all(String type, String id) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.READ);
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startArray();
        Object streams = getResponse.getSource().get("_streams");
        if(streams != null){
            List<Map<String, Object>> _streams = (List<Map<String, Object>>) streams;
            for(Map<String, Object> map : _streams){
                xContentBuilder.startObject();
                Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
                while (it.hasNext()){
                    Map.Entry<String, Object> entry = it.next();
                    xContentBuilder.field(entry.getKey(), entry.getValue());
                }
                xContentBuilder.endObject();
            }
        }
        xContentBuilder.endArray();
        return xContentBuilder;
    }


    public XContentBuilder update(String type, String id, Integer order, List<MultipartFile> files) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.UPDATE);
        List<Map<String, Object>> newStreams = new ArrayList<Map<String, Object>>();
        for(MultipartFile file : files){
            Map<String, Object> page = new HashMap<String, Object>();
            page.put("streamId", UUID.randomUUID().toString());
            page.put("name", file.getName());
            page.put("size", file.getSize());
            page.put("contentType", file.getContentType());
            String fileId = fs.write(file.getBytes());
            if (StringUtils.isBlank(fileId)) {
                throw new uContentException("FS store faild", HttpStatus.NOT_MODIFIED);
            }
            page.put("fileId", fileId);
            newStreams.add(page);
        }
        Object streams = getResponse.getSource().get("_streams");
        List<Map<String, Object>> _streams = null;
        if (streams != null) {
            _streams = (List<Map<String, Object>>) streams;
            _streams.addAll(order, newStreams);
        }else{
            _streams = newStreams;
        }
        Map<String, Object> streamsMap = new HashMap<String, Object>();
        streamsMap.put("_streams", _streams);
        UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), type, id).setDoc(streamsMap).execute().actionGet();
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
        xContentBuilder.field("_index", context.getIndex())
                    .field("_type", type)
                    .field("_id", id)
                    .field("_version", updateResponse.getVersion());
        xContentBuilder.endObject();
        return xContentBuilder;
    }

    public XContentBuilder get(String type, String id, String streamId) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.READ);
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
        Object streams = getResponse.getSource().get("_streams");
        if(streams != null){
            List<Map<String, Object>> _streams = (List<Map<String, Object>>) streams;
            for(Map<String, Object> map : _streams){
                if (map.get("streamId").toString().equals(streamId)) {
                    Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
                    while (it.hasNext()){
                        Map.Entry<String, Object> entry = it.next();
                        xContentBuilder.field(entry.getKey(), entry.getValue());
                    }
                    break;
                }
            }
        }
        xContentBuilder.endObject();
        return xContentBuilder;
    }

    public InputStream getStream(String type, String id, String streamId) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.READ);
        Object streams = getResponse.getSource().get("_streams");
        if (streams != null) {
            List<Map<String, Object>> _streams = (List<Map<String, Object>>) streams;
            for(Map<String, Object> map : _streams){
                if (map.get("streamId").toString().equals(streamId)) {
                    byte[] bytes = fs.read(streamId);
                    return new ByteArrayInputStream(bytes);
                }
            }
        }
        return null;
    }

    public XContentBuilder update(String type, String id, String streamId, MultipartFile file) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.UPDATE);
        Object streams = getResponse.getSource().get("_streams");
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
        xContentBuilder.field("index", context.getIndex())
                .field("type", type)
                .field("id", id);
        if (streams != null) {
            List<Map<String, Object>> _streams = (List<Map<String, Object>>) streams;
            Iterator<Map<String, Object>> it = _streams.iterator();
            while (it.hasNext()){
                Map<String, Object> map = it.next();
                if (map.get("streamId").toString().equals(streamId)) {
                    String fileId = fs.write(file.getBytes());
                    if (StringUtils.isBlank(fileId)) {
                        throw new uContentException("FS store faild", HttpStatus.NOT_MODIFIED);
                    }
                    map.put("name", file.getName());
                    map.put("size", file.getSize());
                    map.put("contentType", file.getContentType());
                    map.put("fileId", fileId);
                    Map<String, Object> streamsMap = new HashMap<String, Object>();
                    streamsMap.put("_streams", _streams);
                    UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), type, id).setDoc(streamsMap).execute().actionGet();
                    xContentBuilder.field("_version", updateResponse.getVersion()).field("_update", true);
                    xContentBuilder.endObject();
                    return xContentBuilder;
                }
            }
        }
        xContentBuilder.field("_version", getResponse.getVersion()).field("_update", false).endObject();
        return xContentBuilder;
    }

    public XContentBuilder update(String type, String id, List<String> removedStreamIds, List<MultipartFile> files) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.UPDATE);
        Object streams = getResponse.getSource().get("_streams");
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
        xContentBuilder.field("index", context.getIndex())
                .field("type", type)
                .field("id", id);
        if(streams != null){
            List<Map<String, Object>> _streams = (List<Map<String, Object>>) streams;
            Iterator<Map<String, Object>> it = _streams.iterator();
            while (it.hasNext()){
                Map<String, Object> map = it.next();
                if (removedStreamIds.contains(map.get("streamId").toString())) {
                    it.remove();
                }
            }
            for(MultipartFile file : files){
                Map<String, Object> stream = new HashMap<String, Object>();
                stream.put("streamId", UUID.randomUUID().toString());
                stream.put("name", file.getName());
                stream.put("size", file.getSize());
                stream.put("contentType", file.getContentType());
                String fileId = fs.write(file.getBytes());
                if (StringUtils.isBlank(fileId)) {
                    throw new uContentException("FS store faild", HttpStatus.NOT_MODIFIED);
                }
                stream.put("fileId", fileId);
                _streams.add(stream);
            }
            Map<String, Object> streamsMap = new HashMap<String, Object>();
            streamsMap.put("_streams", _streams);
            UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), type, id).setDoc(streamsMap).execute().actionGet();
            xContentBuilder.field("_version", updateResponse.getVersion()).field("_update", true);
            xContentBuilder.endObject();
            return xContentBuilder;
        }
        xContentBuilder.field("_version", getResponse.getVersion()).field("_update", false).endObject();
        return xContentBuilder;
    }

    public XContentBuilder delete(String type, String id, String streamId) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.UPDATE);
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
        xContentBuilder.startObject()
                .field("_index", context.getIndex())
                .field("_type", type)
                .field("_id", id);
        Object streams = getResponse.getSource().get("_streams");
        if (streams != null) {
            List<Map<String, Object>> _streams = (List<Map<String, Object>>) streams;
            Iterator<Map<String, Object>> it = _streams.iterator();
            while (it.hasNext()){
                Map<String, Object> entry = it.next();
                if (entry.get("streamId").toString().equals(streamId)) {
                    it.remove();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("_streams", _streams);
                    UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), type, id).setDoc(map).execute().actionGet();
                    xContentBuilder.field("_version", updateResponse.getVersion()).field("_update", true);
                    xContentBuilder.endObject();
                    return xContentBuilder;
                }
            }
        }
        xContentBuilder.field("_version", getResponse.getVersion()).field("_update", false).endObject();
        return xContentBuilder;
    }


}
