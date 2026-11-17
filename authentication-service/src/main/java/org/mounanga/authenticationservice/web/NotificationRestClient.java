package org.mounanga.authenticationservice.web;

import org.mounanga.authenticationservice.configuration.feign.NotificationFeignConfiguration;
import org.mounanga.authenticationservice.dto.NotificationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE", configuration = NotificationFeignConfiguration.class)
public interface NotificationRestClient {

    @PostMapping("/bank/notifications/send")
    void sendNotification(@RequestBody NotificationRequestDTO request);
}
