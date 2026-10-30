package com.parkcore.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
// Disabled STOMP - using raw WebSocket handler instead
// @EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // STOMP disabled
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP disabled - raw WebSocket handler is used instead
    }
}