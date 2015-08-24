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

import java.io.IOException;
import java.util.*;

@Service
public class StreamService {

    @Autowired
    private RequestContext context;

    @Autowired
    private FileSystem fs;

    @Autowired
    private DocumentService documentService;

    public XContentBuilder get(String type, String id) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.READ);
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startArray();
        Object streams = getResponse.getSource().get(Constant.FieldName.STREAMS);
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


    public XContentBuilder get(String type, String id, String streamId) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.READ);
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
        Object streams = getResponse.getSource().get(Constant.FieldName.STREAMS);
        if(streams != null){
            List<Map<String, Object>> _streams = (List<Map<String, Object>>) streams;
            for(Map<String, Object> map : _streams){
                if (map.get(Constant.FieldName.STREAMID).toString().equals(streamId)) {
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


    public Map<String, Object> getStream(String type, String id, String streamId) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.READ);
        Object streams = getResponse.getSource().get(Constant.FieldName.STREAMS);
        if (streams != null) {
            List<Map<String, Object>> _streams = (List<Map<String, Object>>) streams;
            for(Map<String, Object> map : _streams){
                if (map.get(Constant.FieldName.STREAMID).toString().equals(streamId)) {
                    byte[] bytes = fs.read(map.get(Constant.FieldName.ENCODING).toString());
                    if (bytes == null) {
                        throw new uContentException("FS restore failed", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    map.put("bytes", bytes);
                    return map;
                }
            }
        }
        throw new uContentException("Not Found", HttpStatus.NOT_FOUND);
    }


    public XContentBuilder delete(String type, String id, List<String> streamIds) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.UPDATE);
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder();
        xContentBuilder.startObject()
                .field("_index", context.getIndex())
                .field("_type", type)
                .field("_id", id);
        Object streams = getResponse.getSource().get(Constant.FieldName.STREAMS);
        long version = getResponse.getVersion();
        if (streams != null) {
            List<Map<String, Object>> _streams = (List<Map<String, Object>>) streams;
            Iterator<Map<String, Object>> it = _streams.iterator();
            boolean flag = false;
            while (it.hasNext()){
                Map<String, Object> entry = it.next();
                if (streamIds.contains(entry.get(Constant.FieldName.STREAMID).toString())) {
                    it.remove();
                    flag = true;
                }
            }
            if (flag) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(Constant.FieldName.STREAMS, _streams);
                UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), type, id).setDoc(map).execute().actionGet();
                version = updateResponse.getVersion();
            }
        }
        xContentBuilder.field("_version", version).endObject();
        return xContentBuilder;
    }


    public XContentBuilder add(String type, String id, Integer order, List<MultipartFile> files) throws IOException {
        GetResponse getResponse = documentService.checkPermission(type, id, context.getUserName(), Constant.Permission.UPDATE);
        List<Map<String, Object>> newStreams = new ArrayList<Map<String, Object>>();
        for(MultipartFile file : files){
            Map<String, Object> stream = new HashMap<String, Object>();
            stream.put(Constant.FieldName.STREAMID, UUID.randomUUID().toString());
            stream.put(Constant.FieldName.STREAMNAME, file.getName());
            stream.put(Constant.FieldName.LENGTH, file.getSize());
            stream.put(Constant.FieldName.CONTENTTYPE, file.getContentType());
            String fileId = fs.write(file.getBytes());
            if (StringUtils.isBlank(fileId)) {
                throw new uContentException("FS store failed", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            stream.put(Constant.FieldName.ENCODING, fileId);
            newStreams.add(stream);
        }
        Object streams = getResponse.getSource().get(Constant.FieldName.STREAMS);
        List<Map<String, Object>> _streams = null;
        if (streams != null) {
            _streams = (List<Map<String, Object>>) streams;
            if (_streams.size() < order) {
                order = _streams.size();
            }
            _streams.addAll(order, newStreams);
        }else{
            _streams = newStreams;
        }
        Map<String, Object> streamsMap = new HashMap<String, Object>();
        streamsMap.put(Constant.FieldName.STREAMS, _streams);
        UpdateResponse updateResponse = context.getClient().prepareUpdate(context.getIndex(), type, id).setDoc(streamsMap).execute().actionGet();
        XContentBuilder xContentBuilder = JsonXContent.contentBuilder().startObject();
        xContentBuilder.field("_index", context.getIndex())
                    .field("_type", type)
                    .field("_id", id)
                    .field("_version", updateResponse.getVersion());
        xContentBuilder.endObject();
        return xContentBuilder;
    }

}
