package org.mounanga.notificationservice.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationQueueConfiguration {

    private final ApplicationProperties applicationProperties;

    public NotificationQueueConfiguration(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(applicationProperties.getNotificationsQueue())
                .deadLetterExchange("")
                .deadLetterRoutingKey(applicationProperties.getNotificationsDeadLetterQueue())
                .build();
    }

    @Bean
    public Queue notificationsDeadLetterQueue() {
        return QueueBuilder.durable(applicationProperties.getNotificationsDeadLetterQueue()).build();
    }

    @Bean
    public DirectExchange notificationsExchange() {
        return new DirectExchange(applicationProperties.getNotificationsExchange());
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, DirectExchange notificationsExchange) {
        return BindingBuilder.bind(notificationsQueue)
                .to(notificationsExchange)
                .with(applicationProperties.getNotificationsRoutingKey());
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
