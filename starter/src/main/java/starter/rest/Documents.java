package starter.rest;

import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import starter.service.DocumentService;
import starter.uContentException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value="svc/",produces = MediaType.APPLICATION_JSON_VALUE)
public class Documents {

    @Autowired
    private DocumentService documentService;

    @RequestMapping(value = "{type}", method = {RequestMethod.POST}, headers = {"_method=QUERY"})
    public String query(@PathVariable String type,
                        @RequestBody Json query,
                        @RequestParam(defaultValue = "0") int start,
                        @RequestParam(defaultValue = "10") int limit,
                        @RequestParam String sort,
                        @RequestParam(defaultValue = "true") boolean allowableActions) {
        try {
            XContentBuilder result = documentService.query(type, query, start, limit, sort);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "{type}", method = RequestMethod.POST, consumes = "application/json")
    public String createWithoutStream(@PathVariable String type, @RequestBody Json body) {
        try {
            XContentBuilder result = documentService.create(type, body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "{type}", method = RequestMethod.POST, consumes = "multipart/*")
    public String create(@PathVariable String type, MultipartHttpServletRequest request) {
        try {
            MultipartParser parser = new MultipartParser(request).invoke();
            XContentBuilder result = documentService.create(type, parser.getBody(), parser.getFiles());
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "{type}/{id}", method = RequestMethod.HEAD)
    public String head(@PathVariable String type, @PathVariable String id) {
        try {
            XContentBuilder result = documentService.head(type, id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "{type}/{id}", method = RequestMethod.GET)
    public String get(@PathVariable String type,
                      @PathVariable String id,
                      @RequestParam(defaultValue = "true") boolean allowableActions) {
        try {
            XContentBuilder result = documentService.get(type, id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "{type}/{id}", method = RequestMethod.PUT, consumes = "application/json")
    public String updateWithoutStream(@PathVariable String type, @PathVariable String id, @RequestParam Json body) {
        try {
            XContentBuilder result = documentService.update(type, id, body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "{type}/{id}", method = RequestMethod.PATCH, consumes = "application/json")
    public String patchWithoutStream(@PathVariable String type, @PathVariable String id, @RequestParam Json body) {
        try {
            XContentBuilder result = documentService.patch(type, id, body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "{type}/{id}", method = RequestMethod.DELETE)
    public String patch(@PathVariable String type, @PathVariable String id) {
        try {
            XContentBuilder result = documentService.delete(type, id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
