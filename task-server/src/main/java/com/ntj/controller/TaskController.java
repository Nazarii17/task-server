package com.ntj.controller;

import com.ntj.service.TaskLauncherService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskLauncherService taskLauncherService;

    public TaskController(TaskLauncherService taskLauncherService) {
        this.taskLauncherService = taskLauncherService;
    }

    /**
     * Launches a task based on the provided JAR file name and properties.
     *
     * @param jarFileName The name of the JAR file to launch (e.g., config-batch-3.4.1.jar).
     * @param properties  Additional deployment properties for the task.
     */
    @PostMapping("/{jarFileName}")
    public void launchTask(@PathVariable String jarFileName, @RequestBody Map<String, String> properties) {
        properties.put("source", "TASK_CONTROLLER");
        taskLauncherService.launchTask(jarFileName, properties);
    }
}
