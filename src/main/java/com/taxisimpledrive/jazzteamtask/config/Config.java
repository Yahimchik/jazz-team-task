package com.taxisimpledrive.jazzteamtask.config;

import com.taxisimpledrive.jazzteamtask.service.RobotManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Config {

    private final RobotManager robotManager;

    @Scheduled(fixedRate = 5000)
    public void checkRobots() {
        robotManager.checkAndRecreate();
    }
}

