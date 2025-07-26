package com.taxisimpledrive.jazzteamtask.controller;

import com.taxisimpledrive.jazzteamtask.model.Robot;
import com.taxisimpledrive.jazzteamtask.model.Task;
import com.taxisimpledrive.jazzteamtask.service.LogService;
import com.taxisimpledrive.jazzteamtask.service.RobotManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RobotController {

    private final RobotManager robotManager;

    @PostMapping("/task")
    public String sendTask(@RequestBody Task task) {
        robotManager.dispatch(task);
        return "Task dispatched.";
    }

    @GetMapping("/robots")
    public Collection<String> getRobots() {
        return robotManager.getAllRobots()
                .stream()
                .map(Robot::getId)
                .toList();
    }

    @GetMapping("/logs")
    public List<String> getLogs() {
        return LogService.getLogs().stream().toList();
    }
}
