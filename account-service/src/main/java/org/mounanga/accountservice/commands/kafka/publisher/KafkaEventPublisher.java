package org.mounanga.accountservice.commands.kafka.publisher;

import lombok.extern.slf4j.Slf4j;
import org.mounanga.accountservice.commands.kafka.config.KafkaTopicConfig;
import org.mounanga.accountservice.commands.kafka.dto.AccountCreditedKafkaEvent;
import org.mounanga.accountservice.commands.kafka.dto.AccountDebitedKafkaEvent;
import org.mounanga.accountservice.commands.kafka.dto.TransferCompletedKafkaEvent;
import org.mounanga.accountservice.common.event.AccountCreditedEvent;
import org.mounanga.accountservice.common.event.AccountDebitedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAccountCredited(AccountCreditedEvent event) {
        AccountCreditedKafkaEvent kafkaEvent = new AccountCreditedKafkaEvent(
                event.getId(),
                event.getAmount(),
                event.getType().name(),
                event.getDescription(),
                event.getEventBy(),
                event.getEventDate()
        );
        kafkaTemplate.send(KafkaTopicConfig.ACCOUNT_CREDITED_TOPIC, event.getId(), kafkaEvent);
        log.info("Published AccountCredited event to Kafka for account {}", event.getId());
    }

    public void publishAccountDebited(AccountDebitedEvent event) {
        AccountDebitedKafkaEvent kafkaEvent = new AccountDebitedKafkaEvent(
                event.getId(),
                event.getAmount(),
                event.getType().name(),
                event.getDescription(),
                event.getEventBy(),
                event.getEventDate()
        );
        kafkaTemplate.send(KafkaTopicConfig.ACCOUNT_DEBITED_TOPIC, event.getId(), kafkaEvent);
        log.info("Published AccountDebited event to Kafka for account {}", event.getId());
    }

    public void publishTransferCompleted(String fromAccountId, String toAccountId, BigDecimal amount, String description, String eventBy, LocalDateTime eventDate) {
        TransferCompletedKafkaEvent kafkaEvent = new TransferCompletedKafkaEvent(
                fromAccountId,
                toAccountId,
                amount,
                description,
                eventBy,
                eventDate
        );
        kafkaTemplate.send(KafkaTopicConfig.TRANSFER_COMPLETED_TOPIC, fromAccountId, kafkaEvent);
        log.info("Published TransferCompleted event to Kafka from {} to {}", fromAccountId, toAccountId);
    }
}
