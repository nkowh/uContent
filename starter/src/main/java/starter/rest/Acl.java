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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value="svc/",produces = MediaType.APPLICATION_JSON_VALUE)
public class Acl {

    @Autowired
    private AclService aclService;

    @RequestMapping(value = "{type}/{id}/_acl", method = RequestMethod.GET, consumes = "application/json")
    public String all(@PathVariable String type, @PathVariable String id) {
        try {
            List<Map<String, Object>> acls = aclService.all(type, id);
            return XContentBuilderUtils.toXContentBuilder(acls).string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @RequestMapping(value = "{type}/{id}/_acl", method = RequestMethod.PUT, consumes = "application/json")
    public String update(@PathVariable String type, @PathVariable String id, @RequestBody Json body) {
        try {
            Json json = aclService.update(type, id, body);
            return json.toXContentBuilder().string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
