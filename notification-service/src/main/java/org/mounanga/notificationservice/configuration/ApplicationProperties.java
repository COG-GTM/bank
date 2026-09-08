package org.mounanga.notificationservice.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ApplicationProperties {

    @Value("${application.mail.email-system}")
    private String systemEmail;

    @Value("${application.notifications.exchange}")
    private String notificationsExchange;

    @Value("${application.notifications.queue}")
    private String notificationsQueue;

    @Value("${application.notifications.routing-key}")
    private String notificationsRoutingKey;

    @Value("${application.notifications.dead-letter-queue}")
    private String notificationsDeadLetterQueue;
}
