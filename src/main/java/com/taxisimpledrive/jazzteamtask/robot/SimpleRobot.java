package com.taxisimpledrive.jazzteamtask.robot;

import com.taxisimpledrive.jazzteamtask.model.Robot;
import com.taxisimpledrive.jazzteamtask.model.Task;
import com.taxisimpledrive.jazzteamtask.service.LogService;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleRobot implements Robot {

    private static final String LOG_SOURCE = "SimpleRobot";

    private final String id = UUID.randomUUID().toString();
    private final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();

    private volatile boolean active = true;
    private volatile boolean hasWorked = false;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void accept(Task task) {
        taskQueue.offer(task);
        LogService.info(LOG_SOURCE, id + " accepted task: " + task.payload());
    }

    @Override
    public boolean isAlive() {
        return active;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Robot-" + id.substring(0, 8));
        LogService.info(LOG_SOURCE, id + " started.");

        while (active) {
            try {
                Task task = taskQueue.take();
                switch (task.type()) {
                    case WORK -> {
                        LogService.info(LOG_SOURCE, id + " started: " + task.payload());
                        hasWorked = true;
                        Thread.sleep(task.complexity() * 500L);
                        LogService.info(LOG_SOURCE, id + " completed: " + task.payload());
                    }
                    case SUICIDE -> {
                        LogService.warn(LOG_SOURCE, id + " received suicide command, shutting down.");
                        active = false;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LogService.error(LOG_SOURCE, id + " interrupted: " + e.getMessage());
            } catch (Exception e) {
                LogService.error(LOG_SOURCE, id + " failed: " + e.getMessage());
            }
        }

        LogService.info(LOG_SOURCE, id + " stopped.");
    }

    @Override
    public boolean hasWorked() {
        return hasWorked;
    }

    @Override
    public int getTotalComplexity() {
        return taskQueue.stream()
                .mapToInt(Task::complexity)
                .sum();
    }

    @Override
    public boolean isBusy() {
        return !taskQueue.isEmpty();
    }
}
