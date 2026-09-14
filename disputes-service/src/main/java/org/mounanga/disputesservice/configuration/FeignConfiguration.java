package org.mounanga.disputesservice.configuration;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfiguration {

    private static final String AUTHORIZATION = "Authorization";

    @Bean
    public RequestInterceptor authorizationForwardingInterceptor() {
        return template -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
                HttpServletRequest request = attributes.getRequest();
                String header = request.getHeader(AUTHORIZATION);
                if (header != null) {
                    template.header(AUTHORIZATION, header);
                }
            }
        };
    }
}
