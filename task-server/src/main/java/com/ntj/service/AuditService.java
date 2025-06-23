package com.ntj.service;

import com.ntj.model.audit.AuditTask;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.task.repository.TaskExecution;
import org.springframework.cloud.task.repository.TaskExplorer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
@Service
public class AuditService {

    private final TaskExplorer taskExplorer;
    private final AuditTaskPersister auditTaskPersister;

    @Async
    public void createAudit(final String taskName, final UUID customTaskId) {
        System.out.println("--- Initiating Audit for Task ---");
        System.out.println("Task Name: " + taskName);
        System.out.println("Custom Task ID (your generated ID): " + customTaskId);
        System.out.println("---------------------------------");

        final CompletableFuture<TaskExecution> execution = findTaskExecutionByCustomTaskIdAsync(customTaskId, taskName);

        execution.thenAccept(taskExecution -> {
            System.out.println("\n--- Audit Result for Task ---");
            System.out.println("Task Name: " + taskName);
            System.out.println("Custom Task ID: " + customTaskId);
            if (taskExecution != null) {
                System.out.println("Actual Spring Cloud Task ID: " + taskExecution.getExecutionId());

                final AuditTask auditTask = AuditTask.builder()
                        .taskName(taskName)
                        .customTaskId(customTaskId.toString())
                        .taskId(taskExecution.getExecutionId())
                        .build();

                auditTaskPersister.saveAuditTask(auditTask);

            } else {
                System.out.println("Actual Spring Cloud Task ID: Not found within timeout.");
            }
            System.out.println("-----------------------------");

        }).exceptionally(ex -> {
            log.error("Error while finding TaskExecution for customTaskId {}: {}", customTaskId, ex.getMessage(), ex);
            System.err.println("\n--- Audit Error for Task ---");
            System.err.println("Task Name: " + taskName);
            System.err.println("Custom Task ID: " + customTaskId);
            System.err.println("Error: " + ex.getMessage());
            System.err.println("--------------------------");
            return null;
        });
    }

    public CompletableFuture<TaskExecution> findTaskExecutionByCustomTaskIdAsync(final UUID customTaskId,
                                                                                 final String taskName) {
        long startTime = System.currentTimeMillis();
        long timeout = 15000;
        long pollInterval = 1000;

        log.info("Async search for TaskExecution with customTaskId {} and taskName {} for up to {}ms",
                customTaskId, taskName, timeout);

        while (System.currentTimeMillis() - startTime < timeout) {
            final Sort sort = Sort.by(Sort.Direction.DESC, "START_TIME");
            final PageRequest pageRequest = PageRequest.of(0, 100, sort);
            final Page<TaskExecution> taskExecutions = taskExplorer.findAll(pageRequest);

            if (taskExecutions.hasContent()) {
                for (TaskExecution taskExecution : taskExecutions) {
                    log.debug("Checking TaskExecution ID: {}, Name: {}, Arguments: {}",
                            taskExecution.getExecutionId(), taskExecution.getTaskName(), taskExecution.getArguments());

                    if (taskExecution.getTaskName().equals(taskName)) {
                        boolean foundCustomIdArg = taskExecution.getArguments().stream()
                                .anyMatch(arg -> arg.startsWith("--customTaskId=")
                                        && arg.endsWith(customTaskId.toString()));

                        if (foundCustomIdArg) {
                            log.info("Found TaskExecution ID {} for customTaskId {}",
                                    taskExecution.getExecutionId(), customTaskId);
                            return CompletableFuture.completedFuture(taskExecution);
                        }
                    }
                }
            } else {
                log.debug("No task executions found yet by TaskExplorer.");
            }

            try {
                log.debug("TaskExecution for customTaskId {} not found yet. Retrying in {}ms...",
                        customTaskId, pollInterval);
                TimeUnit.MILLISECONDS.sleep(pollInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for task execution for customTaskId: {}", customTaskId, e);
                return CompletableFuture.failedFuture(e);
            }
        }
        log.error("Timed out finding TaskExecution for customTaskId: {} after {}ms.", customTaskId, timeout);
        return CompletableFuture.completedFuture(null);
    }
}
