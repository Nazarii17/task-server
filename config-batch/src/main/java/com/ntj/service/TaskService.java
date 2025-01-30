package com.ntj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.task.repository.TaskExecution;
import org.springframework.cloud.task.repository.TaskExplorer;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class TaskService {

    private final TaskExplorer taskExplorer;

    public Long getCurrentTaskExecutionId() {
        TaskExecution latestTaskExecutionForTaskName = taskExplorer.getLatestTaskExecutionForTaskName("config-batch");
        final long executionId = latestTaskExecutionForTaskName.getExecutionId();

        log.info("Current task execution id is {}", executionId);
        return executionId;
    }
}
