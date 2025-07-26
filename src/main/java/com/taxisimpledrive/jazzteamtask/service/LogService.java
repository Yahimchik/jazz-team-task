package com.taxisimpledrive.jazzteamtask.service;

import com.taxisimpledrive.jazzteamtask.websocket.LogSocketHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


@Service
public class LogService {

    private static final Queue<String> logEntries = new ConcurrentLinkedQueue<>();
    private static LogSocketHandler socketHandler;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                    .withZone(ZoneId.systemDefault());

    @Autowired
    private LogSocketHandler injectedHandler;

    @PostConstruct
    public void init() {
        socketHandler = injectedHandler;
    }

    public static void log(String source, String level, String message) {
        String timestamp = TIME_FORMATTER.format(Instant.now());
        String logLine = String.format("%s [%s] %s - %s", timestamp, level, source, message);
        logEntries.add(logLine);
        System.out.println(logLine);
        if (socketHandler != null) {
            socketHandler.broadcast(logLine);
        }
    }

    public static void info(String source, String message) {
        log(source, "INFO", message);
    }

    public static void error(String source, String message) {
        log(source, "ERROR", message);
    }

    public static void warn(String source, String message) {
        log(source, "WARN", message);
    }

    public static Queue<String> getLogs() {
        return logEntries;
    }
}

