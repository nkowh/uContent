package starter;


import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class uContentException extends RuntimeException {

    private int statusCode;

    public uContentException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode.value();
    }

    public uContentException(String message, Throwable cause, HttpStatus statusCode) {
        super(message, cause);
        this.statusCode = statusCode.value();
    }

    public uContentException(Throwable cause, HttpStatus statusCode) {
        super(cause);
        this.statusCode = statusCode.value();
    }
}
