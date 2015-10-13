package starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.common.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultipartParser {
    private MultipartHttpServletRequest request;
    private Json body;
    private List<MultipartFile> files;
    private ObjectMapper objectMapper = new ObjectMapper();

    public MultipartParser(MultipartHttpServletRequest request) {
        this.request = request;
    }

    public Json getBody() {
        return body;
    }

    public List<MultipartFile> getFiles() {
        return files;
    }

    public MultipartParser invoke() throws IOException {
        body = new Json();
        files = new ArrayList<>();
        Map<String, MultipartFile> fileMap = request.getFileMap();
        for (String name : fileMap.keySet()) {
            for(MultipartFile file : request.getFiles(name)){
                this.files.add(file);
            }
        }
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String key : parameterMap.keySet()) {
            if (key.equals("_acl")) {
                if (StringUtils.isNotBlank(parameterMap.get(key)[0])) {
                    Map<String, Object> acl = objectMapper.readValue(parameterMap.get(key)[0], Map.class);
                    body.put(key, acl);
                }
                continue;
            }
            if (key.equals("_removeStreamIds")) {
                List list = objectMapper.readValue(parameterMap.get(key)[0], List.class);
                body.put(key, list);
                continue;
            }
            body.put(key, parameterMap.get(key)[0]);
        }
        return this;
    }
}