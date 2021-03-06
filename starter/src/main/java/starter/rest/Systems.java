package starter.rest;

import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import starter.RequestContext;
import starter.service.*;
import starter.uContentException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping(value = "svc/", produces = MediaType.APPLICATION_JSON_VALUE)
public class Systems {
    @Autowired
    private TypeService typeService;
    @Autowired
    private UserService userService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private ViewService viewService;
    @Autowired
    private TagService tagService;
    @Autowired
    private RequestContext context;

    @Autowired
    private ReIndexService reIndexService;

    /**
     * *********************** types *****************************
     */

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

    @RequestMapping(value = "types/{ids}", method = RequestMethod.GET)
    public String getType(@PathVariable String[] ids) {
        try {
            XContentBuilder result = typeService.get(ids[0]);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "types/{id}", method = RequestMethod.PUT)
    public String updateType(@PathVariable String id, @RequestBody Json body) {
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


    /**
     * ************************** users *****************************
     */

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
    public String updateUser(@PathVariable String id, @RequestBody Json body) {
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


    /**
     * *************************** groups *****************************
     */

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
    public String updateGroup(@PathVariable String id, @RequestBody Json body) {
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

    @RequestMapping(value = "currentUsers/isAdmin", method = RequestMethod.GET)
    public boolean checkUserInAdminGroup() {
        return groupService.checkUserInAdminGroup();
    }

    /**
     * *************************** views *****************************
     */
    @RequestMapping(value = "views", method = {RequestMethod.GET})
    public String allViews() {
        try {
            XContentBuilder result = Json.parse(viewService.all(), "views");
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "views", method = RequestMethod.POST)
    public String createView(@RequestBody Json body) {
        try {
            XContentBuilder result = viewService.create(body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "views/{id}", method = RequestMethod.GET)
    public String getView(@PathVariable String id) {
        try {
            XContentBuilder result = viewService.get(id).toXContentBuilder();
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "views/{id}", method = RequestMethod.PATCH)
    public String updateView(@PathVariable String id, @RequestBody Json body) {
        try {
            XContentBuilder result = viewService.update(id, body);
            return result.string();
        } catch (IOException e) {
            e.printStackTrace();
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "views/{id}", method = RequestMethod.DELETE)
    public String deleteView(@PathVariable String id) {
        try {
            XContentBuilder result = viewService.delete(id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "views/user/{id}", method = RequestMethod.GET)
    public String getViewsByUser(@PathVariable String id) {
        try {
            XContentBuilder result = Json.parse(viewService.getViewsByUser(id), "views");
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * *************************** tags *****************************
     */
    @RequestMapping(value = "tags", method = {RequestMethod.GET})
    public String allTags() {
        try {
            XContentBuilder result = Json.parse(tagService.all(), "tags");
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "tags", method = RequestMethod.POST)
    public String createTag(@RequestBody Json body) {
        try {
            XContentBuilder result = tagService.create(body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "tags/{id}", method = RequestMethod.GET)
    public String getTag(@PathVariable String id) {
        try {
            XContentBuilder result = tagService.get(id).toXContentBuilder();
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "tags/{id}", method = RequestMethod.PATCH)
    public String updateTag(@PathVariable String id, @RequestBody Json body) {
        try {
            XContentBuilder result = tagService.update(id, body);
            return result.string();
        } catch (IOException e) {
            e.printStackTrace();
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "tags/{id}", method = RequestMethod.DELETE)
    public String deleteTag(@PathVariable String id) {
        try {
            XContentBuilder result = tagService.delete(id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "_reindex", method = RequestMethod.POST)
    public String reindex(@RequestParam(defaultValue = "") String target,
                          @RequestParam(defaultValue = "") String from,
                          @RequestParam(defaultValue = "") String to) {
        try {
            Json json = reIndexService.check();
            if (!((Boolean) json.get("isFinished"))) {
                throw new uContentException("There exist a reindex job which has not finished yet", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Date dateFrom = null;
            Date dateTo = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (StringUtils.isNotBlank(from)) {
                dateFrom = sdf.parse(from);
            }
            if (StringUtils.isNotBlank(to)) {
                dateTo = sdf.parse(to);
            }
            String operationId = new Date().getTime() + "";
            new Thread(new ReIndexService.ReindexJob(context.getClient(), operationId, context.getAlias(), target, dateFrom, dateTo)).start();
            XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject();
            xContentBuilder.field("operationId", operationId)
                    .field("srcIndex", context.getIndex())
                    .field("targetIndex", StringUtils.isNotBlank(target) ? target : ReIndexService.ReindexJob.name(context.getIndex()));
            xContentBuilder.endObject();
            return xContentBuilder.string();
        } catch (ParseException e) {
            throw new uContentException(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            throw new uContentException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "_reindex", method = RequestMethod.GET)
    public String getReindexLog(@RequestParam String operationId,
                                @RequestParam(defaultValue = "30") int size) {
        try {
            return reIndexService.getReindexLog(operationId, size).string();
        } catch (IOException e) {
            throw new uContentException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "_reindex/_status", method = RequestMethod.GET)
    public String reindexStatus() {
        try {
            return reIndexService.check().toXContentBuilder().string();
        } catch (IOException e) {
            throw new uContentException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private void checkAuthorize() {
        if (!groupService.checkUserInAdminGroup()) {
            throw new uContentException("Forbidden", HttpStatus.FORBIDDEN);
        }
    }
}
