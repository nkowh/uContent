package starter.rest;


import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import starter.service.LogService;
import starter.uContentException;

import java.io.IOException;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class Logs {

    @Autowired
    private LogService logService;

    @RequestMapping(value = "logs", method = RequestMethod.GET)
    public String query(@PathVariable String type, @RequestBody Json query,
                        @RequestParam(defaultValue = "0") int start,
                        @RequestParam(defaultValue = "10") int limit,
                        @RequestParam String sort) {
        try {
            XContentBuilder result = logService.query(type, query, start, limit, sort);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
