package starter.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import starter.uContentException;

import java.io.IOException;
import java.util.*;

public class Json extends HashMap<String, Object> {

    public Json() {
    }

    public static Json parse(Map map) {
        ObjectMapper objectMapper = new ObjectMapper();
        Json json = new Json();
        json.putAll(map);
        return json;
    }

    public static Json parse(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonString, Json.class);
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public XContentBuilder toXContentBuilder() throws IOException {
        if (this == null) {
            return null;
        }
        XContentBuilder builder = JsonXContent.contentBuilder();
        builder.startObject();
        Iterator<Entry<String, Object>> it = this.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Object> entry = it.next();
            builder.field(entry.getKey()).value(entry.getValue());
        }
        builder.endObject();
        return builder;
    }

    //将Json集合转换为大Json
    public static XContentBuilder parse(List<Json> list, String arrayName) throws IOException {
        XContentBuilder builder= XContentFactory.jsonBuilder();
        builder.startObject();
        builder.startArray(arrayName);
        if (list == null||list.size()==0) {
            builder.field("total", 0);
        }else{
            builder.field("total", list.size());
            for(Json json:list){
                builder.startObject();
                Iterator<Entry<String, Object>> it = json.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, Object> entry = it.next();
                    builder.field(entry.getKey()).value(entry.getValue());
                }
                builder.endObject();
            }
        }
        builder.endArray();
        builder.endObject();
        return builder;
    }

}
