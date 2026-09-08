package org.mounanga.notificationservice.messaging;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.mounanga.notificationservice.dto.NotificationRequestDTO;
import org.mounanga.notificationservice.service.NotificationService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class NotificationQueueWorkerTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final NotificationService notificationService = mock(NotificationService.class);
    private final NotificationQueueWorker worker = new NotificationQueueWorker(notificationService, validator);

    @Test
    void consumesValidNotification() {
        NotificationRequestDTO dto = new NotificationRequestDTO(
                "recipient@example.com",
                "Subject",
                "Body");

        worker.consume(dto);

        verify(notificationService).send(dto);
    }

    @Test
    void rejectsNotificationWithBlankRecipient() {
        NotificationRequestDTO dto = new NotificationRequestDTO("", "Subject", "Body");

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> worker.consume(dto));

        verifyNoInteractions(notificationService);
    }
}
