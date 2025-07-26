package com.taxisimpledrive.jazzteamtask.model;

import com.taxisimpledrive.jazzteamtask.model.enums.Type;

public record Task(Type type, String payload, String targetRobotId, int complexity) {
    public Task(Type type, String payload, String targetRobotId) {
        this(type, payload, targetRobotId, 1);
    }
}

