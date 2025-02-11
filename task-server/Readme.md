# Task-Server

## Overview

`task-server` is an example application demonstrating the use of Spring Cloud Task and Spring Cloud Batch. It leverages the Spring Cloud Deployer Local to execute tasks packaged as JAR files, enabling both manual execution via API and scheduled execution via Quartz Scheduler.

## Task Execution

### Running Tasks via API

The `task-server` exposes API endpoints to retrieve and execute tasks.

#### Get Available Tasks
```sh
  curl --location 'http://localhost:8080/tasks'
```

#### Run a Task Manually

```sh
  curl --location 'http://localhost:8080/tasks/config-batch' \
--header 'Content-Type: application/json' \
--data '{}'
```

```sh
  curl --location 'http://localhost:8080/tasks/refresh-batch' \
--header 'Content-Type: application/json' \
--data '{}'
```

### Running Tasks on a Schedule

The `task-server` supports Quartz Scheduler for automatic execution of tasks.

#### Quartz Job Definition
```java
@Slf4j
@Component
@RequiredArgsConstructor
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class QuartzTaskRunner implements Job {

    private final TaskLauncherService taskLauncherService;

    @Override
    public void execute(final JobExecutionContext context) {
        final String jobName = context.getJobDetail()
                .getJobDataMap()
                .getString("jobName");
        log.info("\uD83D\uDE80 Running Quartz Job: {}", jobName);
        taskLauncherService.launchTask(jobName, Map.of("source", "QUARTZ_TASK_RUNNER"));
    }
}
```

#### Adding Scheduled Tasks

Tasks can be scheduled by inserting records into the database.
```sql
INSERT INTO task_server_schema.cron_job (job_name, cron_expression) VALUES ('config-batch', '0 0/1 * * * ?');
INSERT INTO task_server_schema.cron_job (job_name, cron_expression) VALUES ('refresh-batch', '0 0/1 * * * ?');
```

## Configuration and Refresh Scope

The `task-server` is integrated with Spring Cloud Config to dynamically update configurations using `@RefreshScope`.

```java
@RefreshScope
@RestController
@RequestMapping("/refresh/message")
public class RefreshMessageController {

    @Value("${application.refresh-message}")
    private String message;

    @GetMapping()
    public String refreshMessage() {
        return message;
    }
}
```

## Packaging and Deploying Tasks

Tasks are packaged as JAR files and copied to the `resources/tasks` directory during the build process using the `maven-resources-plugin`.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-resources-plugin</artifactId>
    <version>3.3.1</version>
    <executions>
        <execution>
            <id>copy-tasks-on-build</id>
            <phase>package</phase>
            <goals>
                <goal>copy-resources</goal>
            </goals>
            <configuration>
                <outputDirectory>${basedir}/src/main/resources/tasks</outputDirectory>
                <resources>
                    <resource>
                        <directory>${main.baser}/config-batch/target</directory>
                        <include>config-batch-*.jar</include>
                    </resource>
                    <resource>
                        <directory>${main.baser}/refresh-batch/target</directory>
                        <include>refresh-batch-*.jar</include>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Understanding Spring Cloud Deployer Local

Spring Cloud Deployer Local is responsible for executing tasks as separate processes. It launches JAR files in isolated JVM instances, ensuring tasks do not interfere with each other. This allows for:
- Independent execution of batch jobs
- Better resource management
- Scalability

The application utilizes the following dependency:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-deployer-local</artifactId>
    <version>2.9.1</version>
</dependency>
```

## Running the Application

Start the `task-server` with:
```sh
  mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Conclusion

The `task-server` demonstrates executing Spring Cloud Tasks dynamically through API calls or scheduled jobs. It integrates with a configuration server and supports runtime property refresh using `@RefreshScope`. Tasks run as separate JVM processes through Spring Cloud Deployer Local, ensuring scalability and isolation.

