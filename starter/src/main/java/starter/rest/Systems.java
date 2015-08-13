package starter.rest;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import starter.service.GroupService;
import starter.service.TypeService;
import starter.service.UserService;
import starter.uContentException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class Systems {
    @Autowired
    private TypeService typeService;
    @Autowired
    private UserService userService;
    @Autowired
    private GroupService groupService;

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

    @RequestMapping(value = "types/{id}", method = RequestMethod.PUT)
    public String updateType(@PathVariable String id,@RequestBody Json body) {
        try {
            XContentBuilder result = typeService.update(id);
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

    @RequestMapping(value = "users", method = RequestMethod.GET)
    public String allUsers() {
        try {
            XContentBuilder result = userService.all();
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "users", method = RequestMethod.POST)
    public String createUser(@RequestBody Json body) {
        try {
            XContentBuilder result = userService.create(body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "users/{id}", method = RequestMethod.GET)
    public String getUser(@PathVariable String id) {
        try {
            XContentBuilder result = userService.get(id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "users/{id}", method = RequestMethod.PUT)
    public String updateUser(@PathVariable String id,@RequestBody Json body) {
        try {
            XContentBuilder result = userService.update(id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "users/{id}", method = RequestMethod.DELETE)
    public String deleteUser(@PathVariable String id) {
        try {
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

    @RequestMapping(value = "groups", method = RequestMethod.GET)
    public String allGroups() {
        try {
            XContentBuilder result = groupService.all();
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "groups", method = RequestMethod.POST)
    public String createGroup(@RequestBody Json body) {
        try {
            XContentBuilder result = groupService.create(body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "groups/{id}/users", method = RequestMethod.POST)
    public String refGroupUsers(@RequestBody List<String> userIds) {
        try {
            XContentBuilder result = groupService.refUsers(userIds);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "groups/{id}", method = RequestMethod.GET)
    public String getGroup(@PathVariable String id) {
        try {
            XContentBuilder result = groupService.get(id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "groups/{id}/users", method = RequestMethod.GET)
    public String getGroupUsers(@PathVariable String id) {
        try {
            XContentBuilder result = groupService.getUsers(id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "groups/{id}", method = RequestMethod.PUT)
    public String updateGroup(@PathVariable String id,@RequestBody Json body) {
        try {
            XContentBuilder result = groupService.update(id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "groups/{id}", method = RequestMethod.DELETE)
    public String deleteGroup(@PathVariable String id) {
        try {
            XContentBuilder result = groupService.delete(id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
