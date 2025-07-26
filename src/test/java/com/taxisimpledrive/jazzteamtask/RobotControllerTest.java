package com.taxisimpledrive.jazzteamtask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxisimpledrive.jazzteamtask.model.Task;
import com.taxisimpledrive.jazzteamtask.model.enums.Type;
import com.taxisimpledrive.jazzteamtask.service.RobotManager;
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
public class RobotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RobotManager robotManager;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
    }

    @Test
    void shouldDispatchMultipleTasksAndScaleUpRobots() throws Exception {
        int initialRobotCount = robotManager.getAllRobots().size();

        for (int i = 1; i <= 100; i++) {
            Task task = new Task(Type.WORK, "Task #" + i, null);
            mockMvc.perform(post("/api/task")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(task)))
                    .andReturn();
        }

        int robotCountAfter = robotManager.getAllRobots().size();

        System.out.println("Initial robots: " + initialRobotCount);
        System.out.println("After dispatching tasks: " + robotCountAfter);

        assertThat(robotCountAfter).isGreaterThanOrEqualTo(initialRobotCount + 1);
    }
}
