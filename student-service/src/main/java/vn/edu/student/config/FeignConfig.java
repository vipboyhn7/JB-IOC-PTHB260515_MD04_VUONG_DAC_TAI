package vn.edu.student.config;

import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.*;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor requestIdInterceptor() {
        return template -> {
            String id = MDC.get("requestId");
            if (id != null) template.header("X-Request-Id", id);
        };
    }
}
