package starter.rest;


import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.sort.SortBuilder;
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
                        @RequestParam(defaultValue = "0") int from,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "[]") SortBuilder[] sort) {
        try {
            XContentBuilder result = logService.query(query, from, size, sort);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


//[{"property":"logDate","direction":"DESC"}]
//%5B%7B%22property%22%3A%22logDate%22,%22direction%22%3A%22DESC%22%7D%5D

//[{"property":"logDate","direction":"ASC"}]
//%5B%7B%22property%22%3A%22logDate%22,%22direction%22%3A%22ASC%22%7D%5D