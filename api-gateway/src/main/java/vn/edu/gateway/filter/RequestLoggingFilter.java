package vn.edu.gateway.filter;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String incoming = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        String requestId = incoming != null && incoming.matches("[A-Za-z0-9._-]{1,64}") ? incoming : UUID.randomUUID().toString();
        var forwarded = exchange.mutate().request(r -> r.headers(h -> h.set("X-Request-Id", requestId))).build();
        forwarded.getResponse().getHeaders().set("X-Request-Id", requestId);
        long start = System.nanoTime();
        log.info("Time={} Method={} URI={} requestId={}", Instant.now(), exchange.getRequest().getMethod(), exchange.getRequest().getPath(), requestId);
        forwarded.getResponse().beforeCommit(() -> {
            var status = forwarded.getResponse().getStatusCode();
            log.info("Gateway end requestId={} status={} durationMs={}", requestId,
                    status == null ? 200 : status.value(), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
            return Mono.empty();
        });
        return chain.filter(forwarded)
                .doOnError(ex -> log.error("Gateway request failed requestId={} path={}", requestId, exchange.getRequest().getPath(), ex))
                .doOnCancel(() -> log.warn("Gateway request cancelled requestId={}", requestId));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
