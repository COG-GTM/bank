package org.mounanga.collectionsservice.web;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "NOTIFICATION-SERVICE", path = "/bank/notifications")
public interface NotificationRestClient {

    @PostMapping("/send")
    void sendNotification(
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body);
}
