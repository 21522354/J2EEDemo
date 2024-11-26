package com.namdam1123.j2ee.postservicecommand;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBusConfig {
    @Bean
    public EventBus eventBus() {
        return SimpleEventBus.builder().build();
    }
}