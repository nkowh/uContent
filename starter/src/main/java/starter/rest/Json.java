package starter.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.springframework.http.HttpStatus;
import starter.uContentException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class Json extends HashMap<String, Object> {

    public static Json parse(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonString, Json.class);
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Json() {
    }

    public XContentBuilder toXContentBuilder() throws IOException {
        if (this == null) {
            return null;
        }
        XContentBuilder builder = JsonXContent.contentBuilder();
        builder.startObject();
        Iterator<Entry<String, Object>> it = this.entrySet().iterator();
        while(it.hasNext()){
            Entry<String, Object> entry = it.next();
            builder.field(entry.getKey()).value(entry.getValue());
        }
        builder.endObject();
        return builder;
    }

}
