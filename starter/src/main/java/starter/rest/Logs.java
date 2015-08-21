package starter.rest;


import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import starter.service.LogService;
import starter.uContentException;

import java.io.IOException;

@RestController
@RequestMapping(value = "svc/", produces = MediaType.APPLICATION_JSON_VALUE)
public class Logs {

    @Autowired
    private LogService logService;

    @RequestMapping(value = "logs", method = {RequestMethod.POST}, headers = {"_method=QUERY"})
    public String query(@RequestBody Json query,
                        @RequestParam(defaultValue = "0") int start,
                        @RequestParam(defaultValue = "10") int limit,
                        @RequestParam String sort) {
        try {
            XContentBuilder result = logService.query(query, start, limit, sort);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
