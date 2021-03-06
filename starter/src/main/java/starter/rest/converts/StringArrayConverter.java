package starter.rest.converts;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import starter.uContentException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class StringArrayConverter implements Converter<String, String[]> {
    @Override
    public String[] convert(String source) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return source.split(",");
        } catch (Exception e) {
            throw new uContentException(String.format("can not convert [%s] to SortBuilder Array", source), HttpStatus.BAD_REQUEST);
        }
    }
}