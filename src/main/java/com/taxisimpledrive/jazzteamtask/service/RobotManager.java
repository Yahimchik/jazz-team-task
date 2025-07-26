package com.taxisimpledrive.jazzteamtask.service;

import com.taxisimpledrive.jazzteamtask.model.Robot;
import com.taxisimpledrive.jazzteamtask.model.Task;
import com.taxisimpledrive.jazzteamtask.robot.SimpleRobot;
import com.taxisimpledrive.jazzteamtask.websocket.RobotSocketHandler;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RobotManager {

    private static final int CAPACITY_PER_ROBOT = 5;
    private static final String LOG_SOURCE = "RobotManager";

    private volatile boolean allTasksCompletedLogged = false;

    private final Map<String, Robot> robots = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final RobotSocketHandler robotSocketHandler;

    public RobotManager(RobotSocketHandler robotSocketHandler) {
        this.robotSocketHandler = robotSocketHandler;
        addRobot(new SimpleRobot());
    }

    /* --- Публичные методы --- */

    public void dispatch(Task task) {
        if (task.targetRobotId() == null) {
            var bestRobot = findBestRobot();
            if (bestRobot == null) bestRobot = createAndAddRobot();
            bestRobot.accept(task);
            checkCapacityAndScale();
        } else {
            Robot robot = robots.get(task.targetRobotId());
            if (robot != null) robot.accept(task);
        }
    }

    public void checkAndRecreate() {
        if (areAnyRobotsBusy()) {
            allTasksCompletedLogged = false;
            return;
        }

        if (robots.size() == 1) {
            if (!allTasksCompletedLogged) {
                LogService.info(LOG_SOURCE, "All tasks finished, kept only one robot: " + robots.keySet().iterator().next());
                allTasksCompletedLogged = true;
            }
            return;
        }

        String robotToKeep = findRobotToKeep();
        if (robotToKeep == null) return;

        robots.keySet().removeIf(id -> !id.equals(robotToKeep));

        LogService.info(LOG_SOURCE, "Removed all robots except: " + robotToKeep);
        broadcastRobotList();

        allTasksCompletedLogged = true;
    }

    public Collection<Robot> getAllRobots() {
        return robots.values();
    }

    /* --- Вспомогательные методы --- */

    private void addRobot(Robot robot) {
        robots.put(robot.getId(), robot);
        executor.submit(robot);
        LogService.info(LOG_SOURCE, "Created robot: " + robot.getId());
        broadcastRobotList();
    }

    private Robot createAndAddRobot() {
        Robot robot = new SimpleRobot();
        addRobot(robot);
        return robot;
    }

    private boolean areAnyRobotsBusy() {
        return robots.values().stream().anyMatch(Robot::isBusy);
    }

    private Robot findBestRobot() {
        return robots.values().stream()
                .min(Comparator.comparingInt(Robot::getTotalComplexity))
                .orElse(null);
    }

    private String findRobotToKeep() {
        return robots.entrySet().stream()
                .filter(e -> e.getValue().hasWorked())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private int getTotalPendingComplexity() {
        return robots.values().stream()
                .mapToInt(Robot::getTotalComplexity)
                .sum();
    }

    private void checkCapacityAndScale() {
        int totalComplexity = getTotalPendingComplexity();
        int capacity = robots.size() * CAPACITY_PER_ROBOT;

        if (totalComplexity > capacity) {
            int neededCapacity = totalComplexity - capacity;
            int toAdd = (neededCapacity + CAPACITY_PER_ROBOT - 1) / CAPACITY_PER_ROBOT;
            for (int i = 0; i < toAdd; i++) {
                createAndAddRobot();
            }
        }
    }

    private void broadcastRobotList() {
        try {
            String robotIds = String.join(",", robots.keySet());
            robotSocketHandler.broadcastRobotList(robotIds);
        } catch (Exception e) {
            LogService.error(LOG_SOURCE, "Failed to broadcast robot list: " + e.getMessage());
        }
    }
}
