package org.mounanga.accountservice.commands.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountCreditedKafkaEvent(
        String accountId,
        BigDecimal amount,
        String operationType,
        String description,
        String eventBy,
        LocalDateTime eventDate
) {}
