package org.mounanga.authenticationservice.configuration.feign;

import feign.RequestInterceptor;
import org.mounanga.authenticationservice.configuration.ApplicationProperties;
import org.springframework.context.annotation.Bean;

public class NotificationFeignConfiguration {

    @Bean
    public RequestInterceptor serviceTokenRequestInterceptor(ApplicationProperties properties) {
        return new ServiceTokenRequestInterceptor(properties.getJwtSecret(), "authentication-service");
    }
}
