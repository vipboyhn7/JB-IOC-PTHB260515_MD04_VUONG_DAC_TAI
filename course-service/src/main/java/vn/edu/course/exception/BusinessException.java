package vn.edu.course.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class BusinessException extends ResponseStatusException {
    private final String code;

    public BusinessException(HttpStatus status, String code, String detail) {
        super(status, detail);
        this.code = code;
    }

    public BusinessException(HttpStatus status, String code, String detail, Throwable cause) {
        super(status, detail, cause);
        this.code = code;
    }
}
