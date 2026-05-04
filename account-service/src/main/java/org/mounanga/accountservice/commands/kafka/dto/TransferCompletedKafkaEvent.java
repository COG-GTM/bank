package org.mounanga.accountservice.commands.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferCompletedKafkaEvent(
        String fromAccountId,
        String toAccountId,
        BigDecimal amount,
        String description,
        String eventBy,
        LocalDateTime eventDate
) {}
