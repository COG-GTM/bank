package org.mounanga.accountservice.queries.web;

import feign.MethodMetadata;
import org.junit.jupiter.api.Test;
import org.mounanga.accountservice.common.feign.NotificationFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringMvcContract;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationRestClientTest {

    @Test
    void sendNotificationTargetsNotificationServiceSendEndpoint() {
        List<MethodMetadata> metadata = new SpringMvcContract().parseAndValidateMetadata(NotificationRestClient.class);

        assertEquals(1, metadata.size());
        assertEquals("POST", metadata.get(0).template().method());
        assertEquals("/bank/notifications/send", metadata.get(0).template().url());
    }

    @Test
    void sendNotificationUsesServiceTokenConfiguration() {
        FeignClient feignClient = NotificationRestClient.class.getAnnotation(FeignClient.class);

        assertArrayEquals(new Class<?>[]{NotificationFeignConfiguration.class}, feignClient.configuration());
    }
}
