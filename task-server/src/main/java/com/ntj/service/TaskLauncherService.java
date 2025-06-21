package com.ntj.service;

import ch.qos.logback.classic.Level;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.cloud.deployer.spi.local.LocalDeployerProperties;
import org.springframework.cloud.deployer.spi.local.LocalTaskLauncher;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskLauncherService {

    private final LocalDeployerProperties localDeployerProperties;
    private final ResourcePatternResolver resourcePatternResolver;
    private final Environment environment;

    @Value("${tasks.location:classpath:tasks/}")
    private String tasksLocation;

    /**
     * Launches a task based on the Task name and deployment properties.
     *
     * @param taskName   The name of the JAR file to launch (e.g., config-batch).
     * @param properties Deployment properties for the task.
     */
    public void launchTask(String taskName, Map<String, String> properties) {
        try {
            if (taskName == null || taskName.isEmpty()) {
                throw new IllegalArgumentException("JAR file name must not be null or empty");
            }
            final Resource taskResource = findResourceByFileName(taskName);
            final String validTaskName = taskName.replace(".jar", "");
            final Map<String, String> deploymentProperties = new HashMap<>(properties);
            final AppDefinition appDefinition = new AppDefinition(validTaskName, deploymentProperties);

            final String activeProfile = Arrays.stream(environment.getActiveProfiles())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Cannot find active profile"));

            final List<String> commandLineArguments = Map.of("spring.profiles.active", activeProfile)
                    .entrySet().stream()
                    .map(entry -> "--" + entry.getKey() + "=" + entry.getValue())
                    .toList();

            final AppDeploymentRequest request = new AppDeploymentRequest(appDefinition, taskResource, deploymentProperties, commandLineArguments);
            final String taskId = new LocalTaskLauncher(localDeployerProperties).launch(request);
            log.info("Task launched with ID: {}", taskId);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to launch task: " + taskName, e);
        }
    }

    /**
     * Finds a resource matching the given JAR file name in the tasks location.
     *
     * @param jarFileName The name of the JAR file to find.
     * @return The resolved resource.
     */
    public Resource findResourceByFileName(final String jarFileName) {
        if (jarFileName == null || jarFileName.isEmpty()) {
            throw new IllegalArgumentException("JAR file name must not be null or empty");
        }
        final List<Resource> taskResources = findAllTaskResources();

        taskResources.forEach(resource ->
                log.info("Found resource: {}", resource.getFilename()));

        return taskResources.stream()
                .filter(resource -> {
                    final String filename = resource.getFilename();
                    return filename != null && filename.contains(jarFileName);
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Resource not found for JAR file: " + jarFileName));
    }

    @SneakyThrows
    private List<Resource> findAllTaskResources() {
        final Resource[] resources = resourcePatternResolver.getResources(tasksLocation + "*.jar");

        return Stream.of(resources).collect(Collectors.toList());
    }

    public List<String> getAllTasks() {
        log.info("Get all tasks");
        final List<String> tasks = findAllTaskResources().stream()
                .map(Resource::getFilename)
                .toList().stream()
                .map(TaskLauncherService::extractBaseName)
                .collect(Collectors.toList());

        addAdditionalLogging();

        log.info("Found tasks: {}", tasks);
        return tasks;
    }

    private void addAdditionalLogging() {
        final Random random = new Random();
        final int randomNumber = random.nextInt(2) + 1;

        if (randomNumber == 1) {
            log.error("Random number is 1");
        }

        if (log instanceof ch.qos.logback.classic.Logger logbackLogger) {

            final Level configuredLevel = logbackLogger.getLevel();
            if (configuredLevel != null) {
                System.out.println("Explicitly configured log level for '" + logbackLogger.getName() + "': " + configuredLevel);
            } else {
                System.out.println("Logger '" + logbackLogger.getName() + "' has no explicit level set. It inherits.");
            }

            final Level effectiveLevel = logbackLogger.getEffectiveLevel();
            System.out.println("Effective log level for '" + logbackLogger.getName() + "': " + effectiveLevel);

            if (Level.ERROR == effectiveLevel) {
                log.error("This is an error level message");
            }
        }
    }

    private static String extractBaseName(String jarFileName) {
        return jarFileName.replaceAll("-\\d+(\\.\\d+)*(-SNAPSHOT)?\\.jar$", "");
    }
}

