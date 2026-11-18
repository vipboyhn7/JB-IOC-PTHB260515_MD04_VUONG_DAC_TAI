package vn.edu.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-2)
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {
    private final ObjectMapper mapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) return Mono.error(ex);
        HttpStatusCode status = ex instanceof ResponseStatusException failure
                ? failure.getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR;
        String requestId = exchange.getResponse().getHeaders().getFirst("X-Request-Id");
        if (requestId == null) requestId = UUID.randomUUID().toString();
        String code = switch (status.value()) {
            case 404 -> "ROUTE_NOT_FOUND";
            case 503 -> "SERVICE_UNAVAILABLE";
            default -> "GATEWAY_HTTP_" + status.value();
        };
        String detail = switch (status.value()) {
            case 404 -> "Không tìm thấy đường dẫn API.";
            case 503 -> "Dịch vụ chưa sẵn sàng. Hãy thử lại sau.";
            default -> "Gateway không thể xử lý yêu cầu. Hãy cung cấp requestId khi liên hệ quản trị viên.";
        };
        if (status.is5xxServerError()) log.error("Gateway error code={} requestId={}", code, requestId, ex);
        else log.warn("Gateway rejected code={} requestId={}", code, requestId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", Objects.requireNonNullElse(HttpStatus.resolve(status.value()), HttpStatus.INTERNAL_SERVER_ERROR).getReasonPhrase());
        body.put("status", status.value());
        body.put("detail", detail);
        body.put("code", code);
        body.put("timestamp", Instant.now().toString());
        body.put("requestId", requestId);
        body.put("instance", exchange.getRequest().getPath().value());
        body.put("errors", List.of());
        try {
            byte[] json = mapper.writeValueAsBytes(body);
            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
            exchange.getResponse().getHeaders().set("X-Request-Id", requestId);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(json)));
        } catch (JsonProcessingException failure) {
            return Mono.error(failure);
        }
    }
}
