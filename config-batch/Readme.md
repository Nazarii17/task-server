# Config-Batch

## Overview
`config-batch` is a Spring Cloud Batch application that consumes configuration data from a config server, processes the consumed data, checks for changes, and writes results to a database. The application demonstrates the use of Spring Cloud Batch with jobs, flows, steps, and execution listeners.

## Configuration
The application retrieves its configurations from a Spring Cloud Config Server.

## Job Definition
The `configBatchJob` processes the application configuration data by:
1. Collecting configuration data.
2. Checking if configurations have changed.
3. Writing the results to the database.

## Steps
### Reader Example
The `AppConfigReader` fetches configuration records from the configured URLs and provides them for processing.

```java
@Slf4j
@Component
public class AppConfigReader implements ItemReader<AppConfigRecord> {

    @Override
    public AppConfigRecord read() {
        return delegate.read();
    }
}
```

### Processor Example
The `ApplicationConfigSnapshotProcessor` processes each `AppConfigRecord` and transforms it into an `AppConfigurationSnapshot`.

```java
@Slf4j
@Component
public class ApplicationConfigSnapshotProcessor implements ItemProcessor<AppConfigRecord, AppConfigurationSnapshot> {

    @Override
    public AppConfigurationSnapshot process(final AppConfigRecord record) {
        log.info("Processing application config snapshot");
        return AppConfigurationSnapshot.builder()
                .appName(record.appName())
                .resourcesData(record.resources())
                .snapshotDateTime(LocalDateTime.now())
                .build();
    }
}
```

### Writer Example
The `AppConfigSnapshotWriter` writes processed snapshots to the database.

```java
@Slf4j
@Component
public class AppConfigSnapshotWriter implements ItemWriter<AppConfigurationSnapshot> {

    @Override
    public void write(Chunk<? extends AppConfigurationSnapshot> chunk) {
        log.info("Writing {} snapshots to database", chunk.size());
        repository.saveAll(chunk);
    }
}
```

## Job Decider and Execution Context Sharing
The `JobDecider` determines whether configurations have changed by retrieving data from the execution context.

```java
@Override
public FlowExecutionStatus decide(final JobExecution jobExecution, final StepExecution stepExecution) {
    final ExecutionContext executionContext = jobExecution.getExecutionContext();
    log.info("In JobDecider isAppsChanged: {}, simpleStepStatus: {}", 
             executionContext.get("isAppsChanged"), 
             executionContext.get("simpleStepStatus"));
    return new FlowExecutionStatus((String) executionContext.get("simpleStepStatus"));
}
```

## Running the Application
1. Ensure the Config Server is running.
2. Start the application using:
   ```sh
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```
3. Monitor job execution logs for batch processing results.