# Refresh-Batch

## Overview
`refresh-batch` is a Spring Cloud Batch application that runs on demand instead of executing at startup. It includes an actuator health check and shuts down automatically after job execution.

## Configuration
The application retrieves its configurations from a Spring Cloud Config Server.

```yaml
cloud:
  config:
    uri: ${application.config-server-base-url}
    name: refresh-batch
    profile: default
    fail-fast: true
    retry:
      max-attempts: 3
      initial-interval: 1000
      multiplier: 2
      max-interval: 5000

server:
  port: 8082

application:
  config-server-base-url: http://localhost:8888
```

## Batch Execution
The application disables batch execution at startup and runs jobs manually through a runner.

```yaml
batch:
  job:
    enabled: false
```

The batch job is triggered in `BatchRunner` and shuts down the application once execution is complete.

```java
@Slf4j
@Component
public class BatchRunner implements CommandLineRunner {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Autowired
    private ApplicationContext context;

    @Override
    public void run(String... args) throws Exception {
        final JobParameters jobParameters = new JobParametersBuilder()
                .addString("id", UUID.randomUUID().toString())
                .toJobParameters();
        final JobExecution execution = jobLauncher.run(job, jobParameters);
        log.info("Job {} status: {}", job.getName(), execution.getStatus());

        if (execution.getExitStatus().getExitCode().equals("COMPLETED")) {
            log.info("Shutting down application...");
            SpringApplication.exit(context, () -> 0);
        }
    }
}
```

## Refresh Scope Example
The application demonstrates the use of `@RefreshScope` to refresh configuration properties dynamically.

```java
@Slf4j
@RefreshScope
@Service
public class RefreshMessageService {
    @Value("${application.refresh-message}")
    private String message;

    public void printRefreshableMessage(final String phase) {
        log.info("- - - \uD83D\uDD04 Refresh message {} context refresh: '{}'", phase, message);
    }
}
```

## Actuator Endpoints
The application triggers refresh endpoints of other batch jobs.

```yaml
batch:
  urls:
    task-server: http://localhost:8080/actuator/refresh
    config-batch: http://localhost:8081/actuator/refresh
    refresh-batch: http://localhost:8082/actuator/refresh
```

## Job Execution Listener
The application fetches updated messages from the `task-server` after a batch job runs.

```java
@Slf4j
public class TaskServerRefreshJobListener implements JobExecutionListener {
    private final RestTemplate restTemplate = new RestTemplate();
    private final static String TASK_SERVER_MESSAGE_URL = "http://localhost:8080/refresh/message";

    @Override
    public void beforeJob(final JobExecution jobExecution) {
        printMessage("Before");
    }

    @Override
    public void afterJob(final JobExecution jobExecution) {
        printMessage("After");
    }

    private void printMessage(final String phase) {
        try {
            final String response = restTemplate.getForObject(TASK_SERVER_MESSAGE_URL, String.class);
            log.info("- - - {} Job Execution -> Task Server message: '{}'", phase, response);
        } catch (Exception e) {
            log.error("Failed to fetch message: {}", e.getMessage());
        }
    }
}
```

## Spring Batch Lifecycle
### Batch Jobs Running on Startup
- When `spring.batch.job.enabled=true`, the batch job automatically executes when the application starts.
- This is useful for processing recurring tasks without manual intervention.
- Example:
  ```yaml
  batch:
    job:
      enabled: true
  ```

### Batch Jobs Running on Demand
- When `spring.batch.job.enabled=false`, the batch job does not start automatically.
- The job is triggered manually via a `CommandLineRunner` or REST endpoint.
- This is useful for ad-hoc batch processing where execution needs to be controlled.
- Example:
  ```yaml
  batch:
    job:
      enabled: false
  ```

## Running the Application
1. Ensure the Config Server is running.
2. Start the application using:
   ```sh
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```
3. Monitor job execution logs for batch processing results.

## Conclusion
The `refresh-batch` application demonstrates an on-demand Spring Batch job that integrates with a config server, dynamically refreshes properties, and ensures batch execution is controlled through manual triggers.

