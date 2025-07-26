package com.taxisimpledrive.jazzteamtask.model;

public interface Robot extends Runnable {
    String getId();

    void accept(Task task);

    boolean isAlive();

    boolean isBusy();

    int getTotalComplexity();

    boolean hasWorked();
}

