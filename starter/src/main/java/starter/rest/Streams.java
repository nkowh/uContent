package starter.rest;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import starter.service.StreamService;
import starter.uContentException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping(value="svc/",produces = MediaType.APPLICATION_JSON_VALUE)
public class Streams {

    @Autowired
    private StreamService streamService;

    @RequestMapping(value = "{type}/{id}/_streams", method = RequestMethod.GET, consumes = "application/json")
    public Object all(@PathVariable String type, @PathVariable String id) {
        try {
            XContentBuilder result = streamService.all(type, id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "{type}/{id}/_streams", method = RequestMethod.PUT, consumes = "multipart/*")
    public Object update(@PathVariable String type, @PathVariable String id,
                         @RequestParam List<String> removedStreamIds,
                         MultipartHttpServletRequest request) {
        try {
            MultipartParser parser = new MultipartParser(request).invoke();
            XContentBuilder result = streamService.update(type, id, removedStreamIds, parser.getFiles());
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "{type}/{id}/_streams", method = RequestMethod.POST, consumes = "multipart/*")
    public Object update(@PathVariable String type, @PathVariable String id,
                         @RequestParam(required = false) Integer order,
                         MultipartHttpServletRequest request) {
        try {
            MultipartParser parser = new MultipartParser(request).invoke();
            XContentBuilder result = streamService.update(type, id, order, parser.getFiles());
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.GET, consumes = "application/json")
    public Object get(@PathVariable String type, @PathVariable String id, @RequestParam String streamId) {
        try {
            XContentBuilder result = streamService.get(type, id, streamId);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.GET, produces = "image/*")
    public void getStream(@PathVariable String type, @PathVariable String id, @RequestParam String streamId, HttpServletResponse response) {
        InputStream stream = null;
        try {
            stream = streamService.getStream(type, id, streamId);
            IOUtils.copy(stream, response.getOutputStream());
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            IOUtils.closeQuietly(stream);

        }
    }

    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.PUT, consumes = "multipart/*")
    public Object update(@PathVariable String type, @PathVariable String id,
                         @RequestParam String streamId,
                         MultipartHttpServletRequest request) {
        try {
            MultipartParser parser = new MultipartParser(request).invoke();
            XContentBuilder result = streamService.update(type, id, streamId, parser.getFiles().get(0));
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.DELETE)
    public Object delete(@PathVariable String type, @PathVariable String id, @RequestParam String streamId) {
        try {
            XContentBuilder result = streamService.delete(type, id, streamId);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
