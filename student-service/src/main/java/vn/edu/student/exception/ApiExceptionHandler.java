package vn.edu.student.exception;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> business(BusinessException ex, HttpServletRequest request) {
        if (ex.getStatusCode().is5xxServerError()) {
            log.error("Business failure code={} path={}", ex.getCode(), request.getRequestURI(), ex);
        } else {
            log.warn("Request rejected code={} status={} path={} detail={}", ex.getCode(), ex.getStatusCode().value(), request.getRequestURI(), ex.getReason());
        }
        return error(ex.getStatusCode(), ex.getCode(), ex.getReason(), request.getRequestURI(), List.of());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var fields = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldError(e.getField(), e.getDefaultMessage())).toList();
        log.warn("Validation failed path={} fields={}", path(request), fields);
        return error(status, "VALIDATION_ERROR", "Dữ liệu không hợp lệ. Hãy kiểm tra các trường được liệt kê.", path(request), fields);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Malformed JSON path={}", path(request));
        return error(status, "INVALID_JSON", "JSON không hợp lệ, sai kiểu dữ liệu hoặc thiếu request body.", path(request), List.of());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode status, WebRequest request) {
        log.warn("HTTP error status={} path={} type={}", status.value(), path(request), ex.getClass().getSimpleName());
        ProblemDetail problem = problem(status, "HTTP_" + status.value(),
                status.is5xxServerError() ? "Lỗi xử lý yêu cầu." : "Yêu cầu không hợp lệ: kiểm tra URL, HTTP method và kiểu dữ liệu.", path(request), List.of());
        return new ResponseEntity<>(problem, headers, status);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> conflict(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data constraint violation path={}", request.getRequestURI());
        return error(HttpStatus.CONFLICT, "DATA_CONFLICT", "Dữ liệu bị trùng hoặc vi phạm ràng buộc. Kiểm tra email và dữ liệu đã tồn tại.", request.getRequestURI(), List.of());
    }

    @ExceptionHandler(TransientDataAccessException.class)
    public ResponseEntity<Object> databaseBusy(TransientDataAccessException ex, HttpServletRequest request) {
        log.error("Database temporarily unavailable path={}", request.getRequestURI(), ex);
        return error(HttpStatus.SERVICE_UNAVAILABLE, "DATABASE_BUSY", "Database đang bận hoặc tạm thời không sẵn sàng. Hãy thử lại.", request.getRequestURI(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> unexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error path={}", request.getRequestURI(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Lỗi hệ thống. Liên hệ quản trị viên và cung cấp requestId.", request.getRequestURI(), List.of());
    }

    private ResponseEntity<Object> error(HttpStatusCode status, String code, String detail, String path, List<FieldError> fields) {
        return ResponseEntity.status(status).body(problem(status, code, detail, path, fields));
    }

    private ProblemDetail problem(HttpStatusCode status, String code, String detail, String path, List<FieldError> fields) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setInstance(URI.create(path));
        problem.setProperty("code", code);
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("requestId", MDC.get("requestId"));
        problem.setProperty("errors", fields);
        return problem;
    }

    private String path(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }

    public record FieldError(String field, String message) {
    }
}
