package org.mounanga.collectionsservice.configuration;

import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfiguration {

    @Autowired
    public void configure(EventProcessingConfigurer configurer) {
        configurer.usingSubscribingEventProcessors();
    }
}
