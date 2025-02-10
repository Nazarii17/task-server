package com.ntj.controller;

import com.ntj.service.TaskLauncherService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskLauncherService taskLauncherService;

    public TaskController(TaskLauncherService taskLauncherService) {
        this.taskLauncherService = taskLauncherService;
    }

    /**
     * Launches a task based on the provided Task name and properties.
     *
     * @param taskName The name of the Task to launch (e.g., config-batch).
     * @param properties  Additional deployment properties for the task.
     */
    @PostMapping("/{taskName}")
    public void launchTask(@PathVariable String taskName, @RequestBody Map<String, String> properties) {
        if (Objects.isNull(properties)) {
            properties = new HashMap<>();
        }
        properties.put("source", "TASK_CONTROLLER");
        taskLauncherService.launchTask(taskName, properties);
    }

    @GetMapping()
    public List<String> getAllTasks() {
        return taskLauncherService.getAllTasks();
    }
}
