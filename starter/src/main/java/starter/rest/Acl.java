package starter.rest;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import starter.service.AclService;
import starter.service.StreamService;
import starter.uContentException;

import java.io.IOException;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class Acl {

    @Autowired
    private AclService aclService;

    @RequestMapping(value = "{type}/{id}/_acl", method = RequestMethod.GET, consumes = "application/json")
    public String all(@PathVariable String type, @PathVariable String id) {
        try {
            XContentBuilder result = aclService.all(type, id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @RequestMapping(value = "{type}/{id}/_acl", method = RequestMethod.PUT, consumes = "application/json")
    public String update(@PathVariable String type, @PathVariable String id, @RequestBody Json body) {
        try {
            XContentBuilder result = aclService.update(type, id, body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
