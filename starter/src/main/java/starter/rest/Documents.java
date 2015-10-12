package starter.rest;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import starter.service.DocumentService;
import starter.uContentException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/svc", produces = MediaType.APPLICATION_JSON_VALUE)
public class Documents {

    @Autowired
    private DocumentService documentService;


    @RequestMapping(value = "/{type}", method = {RequestMethod.GET})
    public String query(@PathVariable String type,
                        @RequestParam(defaultValue = "") String query,
                        @RequestParam(defaultValue = "false") boolean fulltext,
                        @RequestParam(defaultValue = "0") int start,
                        @RequestParam(defaultValue = "10") int limit,
                        @RequestParam(defaultValue = "[]") SortBuilder[] sort,
                        @RequestParam(defaultValue = "false") boolean allowableActions) {
        try {
            query = java.net.URLDecoder.decode(query, "UTF-8");
            String[] types = {type};
            XContentBuilder xContentBuilder = documentService.query(types, query, start, limit, sort, allowableActions, fulltext);
            return xContentBuilder.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "", method = {RequestMethod.GET})
    public String all(@RequestParam(defaultValue = "") String[] types,
                      @RequestParam(defaultValue = "") String query,
                      @RequestParam(defaultValue = "false") boolean highlight,
                      @RequestParam(defaultValue = "0") int start,
                      @RequestParam(defaultValue = "10") int limit,
                      @RequestParam(defaultValue = "[]") SortBuilder[] sort,
                      @RequestParam(defaultValue = "false") boolean allowableActions) {
        try {
            XContentBuilder xContentBuilder = documentService.query(types, query, start, limit, sort, allowableActions, highlight);
            return xContentBuilder.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{type}", method = RequestMethod.POST, consumes = "application/json")
    public String createWithoutStream(@PathVariable String type, @RequestBody Json body) {
        try {
            XContentBuilder result = documentService.create(type, body);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ParseException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{type}", method = RequestMethod.POST, consumes = "multipart/*")
    public String create(@PathVariable String type, MultipartHttpServletRequest request) {
        try {
            MultipartParser parser = new MultipartParser(request).invoke();
            XContentBuilder result = documentService.create(type, parser.getBody(), parser.getFiles());
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ParseException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{type}/{id}", method = RequestMethod.GET)
    public String get(@PathVariable String type,
                      @PathVariable String id,
                      @RequestParam(defaultValue = "false") boolean allowableActions) {
        try {
            Json json = documentService.get(type, id, false, allowableActions);
            return json.toXContentBuilder().string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{type}/{id}", method = RequestMethod.PUT, consumes = "application/json")
    public String updateWithoutStream(@PathVariable String type, @PathVariable String id, @RequestBody Json body) {
        try {
            XContentBuilder xContentBuilder = documentService.update(type, id, body);
            return xContentBuilder.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ParseException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{type}/{id}", method = RequestMethod.PATCH, consumes = "application/json")
    public String patchWithoutStream(@PathVariable String type, @PathVariable String id, @RequestBody Json body) {
        try {
            XContentBuilder xContentBuilder = documentService.patch(type, id, body);
            return xContentBuilder.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ParseException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{type}/{id}", method = RequestMethod.PUT, consumes = "multipart/*")
    public String patchWithStream(@PathVariable String type, @PathVariable String id, MultipartHttpServletRequest request) {
        try {
            MultipartParser parser = new MultipartParser(request).invoke();
            XContentBuilder result = documentService.update(type, id, parser.getBody(), parser.getFiles());
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ParseException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{type}/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable String type, @PathVariable String id) {
        try {
            XContentBuilder xContentBuilder = documentService.delete(type, id);
            return xContentBuilder.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public String delete(@RequestBody List<Map> body) {
        XContentBuilder xContentBuilder = documentService.delete(body);
        try {
            return xContentBuilder.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
