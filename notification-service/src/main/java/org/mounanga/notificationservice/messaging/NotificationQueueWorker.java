package org.mounanga.notificationservice.messaging;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.mounanga.notificationservice.dto.NotificationRequestDTO;
import org.mounanga.notificationservice.service.NotificationService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
public class NotificationQueueWorker {

    private final NotificationService notificationService;
    private final Validator validator;

    public NotificationQueueWorker(NotificationService notificationService, Validator validator) {
        this.notificationService = notificationService;
        this.validator = validator;
    }

    @RabbitListener(queues = "${application.notifications.queue}")
    public void consume(NotificationRequestDTO dto) {
        Set<ConstraintViolation<NotificationRequestDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            log.warn("Rejecting invalid notification message: {}", violations);
            throw new AmqpRejectAndDontRequeueException("Invalid notification payload");
        }
        notificationService.send(dto);
    }
}
