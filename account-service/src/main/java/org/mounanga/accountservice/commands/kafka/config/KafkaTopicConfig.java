package org.mounanga.accountservice.commands.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String ACCOUNT_CREDITED_TOPIC = "account-credited";
    public static final String ACCOUNT_DEBITED_TOPIC = "account-debited";
    public static final String TRANSFER_COMPLETED_TOPIC = "transfer-completed";

    @Bean
    public NewTopic accountCreditedTopic() {
        return TopicBuilder.name(ACCOUNT_CREDITED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic accountDebitedTopic() {
        return TopicBuilder.name(ACCOUNT_DEBITED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transferCompletedTopic() {
        return TopicBuilder.name(TRANSFER_COMPLETED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
