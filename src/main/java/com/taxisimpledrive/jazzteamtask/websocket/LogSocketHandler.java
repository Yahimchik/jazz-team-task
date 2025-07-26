package com.taxisimpledrive.jazzteamtask.websocket;

import com.taxisimpledrive.jazzteamtask.service.LogService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Slf4j
public class LogSocketHandler implements WebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    public LogSocketHandler() {
        Thread sender = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String message = messageQueue.take();

                    for (WebSocketSession session : sessions) {
                        try {
                            if (session.isOpen()) {
                                session.sendMessage(new TextMessage(message));
                            }
                        } catch (IOException e) {
                            LogService.error("Error sending message {}", e.getMessage());
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "LogSocketSender");
        sender.setDaemon(true);
        sender.start();
    }

    public void broadcast(String message) {
        messageQueue.offer(message);
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) {
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) {
        sessions.remove(session);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) {
        sessions.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
