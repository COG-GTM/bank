package org.mounanga.accountservice.common.feign;

import feign.RequestInterceptor;
import org.mounanga.accountservice.common.properties.ApplicationProperties;
import org.springframework.context.annotation.Bean;

public class NotificationFeignConfiguration {

    @Bean
    public RequestInterceptor serviceTokenRequestInterceptor(ApplicationProperties properties) {
        return new ServiceTokenRequestInterceptor(properties.getJwtSecret(), "account-service");
    }
}
