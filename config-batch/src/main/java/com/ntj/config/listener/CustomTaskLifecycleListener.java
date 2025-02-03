package com.ntj.config.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.task.listener.annotation.BeforeTask;
import org.springframework.cloud.task.listener.annotation.AfterTask;
import org.springframework.cloud.task.repository.TaskExecution;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomTaskLifecycleListener {

    private long startTime;

    @BeforeTask
    public void beforeTask(TaskExecution taskExecution) {
        startTime = System.currentTimeMillis();
        log.info("Task started: Task Name = {}, Execution ID = {}",
                taskExecution.getTaskName(), taskExecution.getExecutionId());
    }

    @AfterTask
    public void afterTask(TaskExecution taskExecution) {
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        log.info("Task completed: Task Name = {}, Execution ID = {}, Exit Code = {}, Execution Time = {} ms",
                taskExecution.getTaskName(),
                taskExecution.getExecutionId(),
                taskExecution.getExitCode(),
                executionTime);
    }
}

