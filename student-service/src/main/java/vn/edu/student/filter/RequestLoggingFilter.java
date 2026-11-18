package vn.edu.student.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null || !requestId.matches("[A-Za-z0-9._-]{1,64}")) requestId = UUID.randomUUID().toString();
        long start = System.nanoTime();
        try (var ignored = MDC.putCloseable("requestId", requestId)) {
            response.setHeader("X-Request-Id", requestId);
            log.info("HTTP start Time={} Method={} URI={}", Instant.now(), request.getMethod(), request.getRequestURI());
            chain.doFilter(request, response);
        } finally {
            log.info("HTTP end requestId={} Method={} URI={} status={} durationMs={}", requestId,
                    request.getMethod(), request.getRequestURI(), response.getStatus(), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
        }
    }
}
