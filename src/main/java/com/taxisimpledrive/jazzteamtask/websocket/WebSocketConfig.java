package com.taxisimpledrive.jazzteamtask.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final LogSocketHandler logSocketHandler;
    private final RobotSocketHandler robotSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(logSocketHandler, "/ws/logs").setAllowedOrigins("*");
        registry.addHandler(robotSocketHandler, "/ws/robots").setAllowedOrigins("*");
    }
}
