package starter.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import starter.uContentException;

import java.io.IOException;
import java.util.HashMap;

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


}
