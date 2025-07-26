package com.taxisimpledrive.jazzteamtask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxisimpledrive.jazzteamtask.model.Task;
import com.taxisimpledrive.jazzteamtask.model.enums.Type;
import com.taxisimpledrive.jazzteamtask.service.RobotManager;
import com.taxisimpledrive.jazzteamtask.websocket.RobotSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class RobotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RobotManager robotManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RobotSocketHandler robotSocketHandler;

    @BeforeEach
    void setup() {
        // Можно почистить список задач или пересоздать роботов, если нужно
    }

    @Test
    void shouldAddNewRobotIfNoneHasCapacity() throws Exception {
        int initialCount = robotManager.getAllRobots().size();

        for (int i = 1; i <= 30; i++) {
            Task task = new Task(Type.WORK, "Task #" + i, null);
            mockMvc.perform(post("/api/task")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(task)))
                    .andReturn();
        }

        int currentCount = robotManager.getAllRobots().size();

        assertThat(currentCount).isGreaterThan(initialCount);
    }

    @Test
    void shouldTargetSpecificRobotWhenTaskHasTargetId() throws Exception {
        String targetRobotId = robotManager.getAllRobots().stream().findFirst().orElseThrow().getId();
        int initialComplexity = robotManager.getAllRobots().stream()
                .filter(r -> r.getId().equals(targetRobotId))
                .findFirst().orElseThrow()
                .getTotalComplexity();

        Task task = new Task(Type.WORK, "Direct Task", targetRobotId);

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andReturn();

        // Даем время задаче быть обработанной
        TimeUnit.MILLISECONDS.sleep(300);

        int afterComplexity = robotManager.getAllRobots().stream()
                .filter(r -> r.getId().equals(targetRobotId))
                .findFirst().orElseThrow()
                .getTotalComplexity();

        assertThat(afterComplexity).isGreaterThanOrEqualTo(initialComplexity);
    }

    @Test
    void shouldCleanUpToOneRobotWhenIdle() throws Exception {
        for (int i = 1; i <= 20; i++) {
            Task task = new Task(Type.WORK, "Task " + i, null);
            mockMvc.perform(post("/api/task")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(task)))
                    .andReturn();
        }

        // Ждем, пока все задачи будут завершены
        TimeUnit.SECONDS.sleep(5);
        robotManager.checkAndRecreate();

        assertThat(robotManager.getAllRobots().size()).isEqualTo(1);
    }
}
