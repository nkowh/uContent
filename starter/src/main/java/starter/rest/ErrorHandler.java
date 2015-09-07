package starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import starter.uContentException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice(annotations = RestController.class)
public class ErrorHandler {

    @ExceptionHandler(uContentException.class)
    public void handle(uContentException ex,HttpServletResponse response) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("status", ex.getStatusCode());
        map.put("reason", ex.getMessage());
        ObjectMapper mapper=new ObjectMapper();
        response.setStatus(ex.getStatusCode());
        response.setContentType("application/json;charset=UTF-8");
        IOUtils.write(mapper.writeValueAsBytes(map),response.getOutputStream());

    }

}
