package starter.rest;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import starter.service.Constant;
import starter.service.StreamService;
import starter.uContentException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "svc/", produces = MediaType.APPLICATION_JSON_VALUE)
public class Streams {

    @Autowired
    private StreamService streamService;

    @RequestMapping(value = "{type}/{id}/_streams", method = RequestMethod.GET)
    public Object get(@PathVariable String type, @PathVariable String id) {
        try {
            XContentBuilder result = streamService.get(type, id);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.GET)
    public Object get(@PathVariable String type, @PathVariable String id, @PathVariable String streamId) {
        try {
            XContentBuilder result = streamService.get(type, id, streamId);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.GET, params = "accept=image/jpeg")
    public void geJpeg(@PathVariable String type, @PathVariable String id, @PathVariable String streamId, HttpServletResponse response) {
        getStream(type, id, streamId, response);
    }

    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.GET, params = "accept=image/png")
    public void getPng(@PathVariable String type, @PathVariable String id, @PathVariable String streamId, HttpServletResponse response) {
        getStream(type, id, streamId, response);
    }

    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.GET, params = "accept=image/tiff")
    public void getTiff(@PathVariable String type, @PathVariable String id, @PathVariable String streamId, @RequestParam(defaultValue = "0") int pageIndex, HttpServletResponse response) {
        InputStream stream = null;
        try {
            Map<String, Object> result = streamService.getStream(type, id, streamId);
            stream = new ByteArrayInputStream((byte[]) result.get("bytes"));
            ImageReader reader = ImageIO.getImageReadersByFormatName("tif").next();
            reader.setInput(ImageIO.createImageInputStream(stream));
            int pageCount = reader.getNumImages(true);
            BufferedImage image = reader.read(pageIndex);
            if (image.getColorModel().getPixelSize() > 8) {
                response.setContentType("image/jpeg");
                ImageIO.write(image, "jpeg", response.getOutputStream());
            } else {
                response.setContentType("image/png");
                ImageIO.write(image, "png", response.getOutputStream());
            }

            // response.setContentLength(Integer.valueOf(result.get(Constant.FieldName.LENGTH).toString()));
            //stream = new ByteArrayInputStream((byte[]) result.get("bytes"));
            //IOUtils.copy(stream, response.getOutputStream());
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            IOUtils.closeQuietly(stream);

        }
    }

    @RequestMapping(value = "{type}/{id}/_streams/{streamId}", method = RequestMethod.GET, produces = "image/*")
    public void getStream(@PathVariable String type, @PathVariable String id, @PathVariable String streamId, HttpServletResponse response) {
        InputStream stream = null;
        try {
            Map<String, Object> result = streamService.getStream(type, id, streamId);
            response.setContentType(result.get(Constant.FieldName.CONTENTTYPE).toString());
            response.setContentLength(Integer.valueOf(result.get(Constant.FieldName.LENGTH).toString()));
            stream = new ByteArrayInputStream((byte[]) result.get("bytes"));
            IOUtils.copy(stream, response.getOutputStream());
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            IOUtils.closeQuietly(stream);

        }
    }


    @RequestMapping(value = "{type}/{id}/_streams", method = RequestMethod.DELETE, consumes = "application/json")
    public Object delete(@PathVariable String type, @PathVariable String id, @RequestBody List<String> streamIds) {
        try {
            XContentBuilder result = streamService.delete(type, id, streamIds);
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "{type}/{id}/_streams", method = RequestMethod.POST, consumes = "multipart/*")
    public Object add(@PathVariable String type, @PathVariable String id,
                      @RequestParam(defaultValue = "0") Integer order,
                      MultipartHttpServletRequest request) {
        try {
            MultipartParser parser = new MultipartParser(request).invoke();
            XContentBuilder result = streamService.add(type, id, order, parser.getFiles());
            return result.string();
        } catch (IOException e) {
            throw new uContentException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
