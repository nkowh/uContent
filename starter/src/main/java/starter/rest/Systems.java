package starter.rest;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import starter.service.GroupService;
import starter.service.ReIndexService;
import starter.service.TypeService;
import starter.service.UserService;
import starter.uContentException;

import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping(value="svc/",produces = MediaType.APPLICATION_JSON_VALUE)
public class Systems {
    @Autowired
    private TypeService typeService;
    @Autowired
    private UserService userService;
    @Autowired
    private GroupService groupService;

    @Autowired
    private ReIndexService reIndexService;

    /************************** types ******************************/

    @RequestMapping(value = "types", method = RequestMethod.GET)
    public String allTypes() {
        try {
            XContentBuilder result = typeService.all();
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "types", method = RequestMethod.POST)
    public String createType(@RequestBody Json body) {
        try {
            XContentBuilder result = typeService.create(body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "types/{id}", method = RequestMethod.GET)
    public String getType(@PathVariable String id) {
        try {
            XContentBuilder result = typeService.get(id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "types/{id}", method = RequestMethod.PATCH)
    public String updateType(@PathVariable String id,@RequestBody Json body) {
        try {
            XContentBuilder result = typeService.update(id, body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "types/{id}", method = RequestMethod.DELETE)
    public String deleteType(@PathVariable String id) {
        try {
            XContentBuilder result = typeService.delete(id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /***************************** users ******************************/

    @RequestMapping(value = "users", method = {RequestMethod.GET})
    public String allUsers(@RequestParam(defaultValue = "") String query,
                           @RequestParam(defaultValue = "0") int start,
                           @RequestParam(defaultValue = "10") int limit,
                           @RequestParam(defaultValue = "[]") SortBuilder[] sort) {
        try {
            XContentBuilder result = userService.all(query, start, limit, sort);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "users", method = RequestMethod.POST)
    public String createUser(@RequestBody Json body) {
        try {
            checkAuthorize();
            XContentBuilder result = userService.create(body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "users/{id}", method = RequestMethod.GET)
    public String getUser(@PathVariable String id) {
        try {
            checkAuthorize();
            XContentBuilder result = userService.get(id).toXContentBuilder();
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "users/{id}", method = RequestMethod.PATCH)
    public String updateUser(@PathVariable String id,@RequestBody Json body) {
        try {
            checkAuthorize();
            XContentBuilder result = userService.update(id, body);
            return result.string();
        } catch (IOException e) {
            e.printStackTrace();
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "users/{id}", method = RequestMethod.DELETE)
    public String deleteUser(@PathVariable String id) {
        try {
            checkAuthorize();
            XContentBuilder result = userService.delete(id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "users/{id}/groups", method = RequestMethod.GET)
    public String getUserGroups(@PathVariable String id) {
        try {
            XContentBuilder result = userService.getGroups(id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /****************************** groups ******************************/

    @RequestMapping(value = "groups", method = {RequestMethod.GET})
    public String allGroups(@RequestParam(defaultValue = "") String query,
                            @RequestParam(defaultValue = "0") int start,
                            @RequestParam(defaultValue = "10") int limit,
                            @RequestParam(defaultValue = "[]") SortBuilder[] sort) {
        try {
            XContentBuilder result = groupService.all(query, start, limit, sort);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "groups", method = RequestMethod.POST)
    public String createGroup(@RequestBody Json body) {
        try {
            checkAuthorize();
            XContentBuilder result = groupService.create(body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "groups/{id}/users", method = RequestMethod.PATCH)
    public String refGroupUsers(@PathVariable String id,
                                 @RequestBody Json userIds) {
        try {
            checkAuthorize();
            XContentBuilder result = groupService.refUsers(id, userIds);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "groups/{id}", method = RequestMethod.GET)
    public String getGroup(@PathVariable String id) {
        try {
            checkAuthorize();
            XContentBuilder result = groupService.get(id).toXContentBuilder();
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "groups/{id}/users", method = RequestMethod.GET)
    public String getGroupUsers(@PathVariable String id) {
        try {
            checkAuthorize();
            XContentBuilder result = groupService.getUsers(id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "groups/{id}", method = RequestMethod.PATCH)
    public String updateGroup(@PathVariable String id,@RequestBody Json body) {
        try {
            checkAuthorize();
            XContentBuilder result = groupService.update(id, body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "groups/{id}", method = RequestMethod.DELETE)
    public String deleteGroup(@PathVariable String id) {
        try {
            checkAuthorize();
            XContentBuilder result = groupService.delete(id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "system", method = RequestMethod.POST)
    public void systemDataInitial() {
        try {
            userService.initialUserData();
            groupService.initialGroupData();
        } catch (Exception e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "_reIndex", method = RequestMethod.POST)
    public void reindex() {
        reIndexService.reIndex();
    }


    private void checkAuthorize(){
        if(!groupService.checkUserInAdminGroup()){
            throw new uContentException("Forbidden", HttpStatus.FORBIDDEN);
        }
    }
}
