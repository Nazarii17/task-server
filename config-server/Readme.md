# Spring Cloud Config Server

Spring Cloud Config Server provides a centralized and externalized configuration management system for distributed applications. It allows applications to fetch configuration properties dynamically, ensuring consistency across environments.

## Why Use Spring Cloud Config?
- Centralized configuration management.
- Dynamic updates without redeploying applications.
- Support for multiple environments (local, dev, prod, etc.).
- Configuration versioning with Git or other storage backends.
- Secure storage of sensitive properties.

## Configuration Setup
To use Spring Cloud Config Server, applications should include the following properties:

```yaml
application:
  config-server-base-url: http://localhost:8888

spring:
  application:
    name: config-batch
  config:
    import: optional:configserver:${application.config-server-base-url}
  cloud:
    config:
      uri: ${application.config-server-base-url}
      name: config-batch
      profile: default
      fail-fast: true
      retry:
        max-attempts: 3
        initial-interval: 1000
        multiplier: 2
        max-interval: 5000
```

### Explanation:
- `config-server-base-url` defines the URL of the Config Server.
- `spring.application.name` should match the application name used in the configuration files.
- `spring.cloud.config.uri` points to the Config Server endpoint.
- `fail-fast` ensures the application fails immediately if configuration retrieval fails.
- `retry` settings control the number of retry attempts for fetching configurations.

## Using `@RefreshScope`
Spring provides the `@RefreshScope` annotation to dynamically reload configuration properties without restarting the application. To enable it, annotate your Spring component:

```java
@RefreshScope
@RestController
public class ConfigController {
    
    @Value("${some.config.property}")
    private String property;

    @GetMapping("/config-property")
    public String getConfigProperty() {
        return property;
    }
}
```

To refresh properties at runtime, send a request to the actuator refresh endpoint:

```sh
  curl -X POST http://localhost:8080/actuator/refresh
```

## Available APIs
Spring Cloud Config Server exposes APIs to fetch configuration files based on application name and environment:

### TASK-SERVER

```sh
  curl --location 'http://localhost:8888/task-server/local'
```
```sh
  curl --location 'http://localhost:8888/task-server/dev'
```
```sh
  curl --location 'http://localhost:8888/task-server/prod'
```

### CONFIG-BATCH

```sh
  curl --location 'http://localhost:8888/config-batch/local'
```
```sh
  curl --location 'http://localhost:8888/config-batch/dev'
```
```sh
  curl --location 'http://localhost:8888/config-batch/prod'
```
### REFRESH-BATCH
```sh
  curl --location 'http://localhost:8888/refresh-batch/local'
```
```sh
  curl --location 'http://localhost:8888/refresh-batch/dev'
```
```sh
  curl --location 'http://localhost:8888/refresh-batch/prod'
```

### Health Check
Check the health status of the Config Server:

```sh
  curl --location 'http://localhost:8888/actuator/health'
```

## How It Works
1. The Config Server serves configuration files from a repository (e.g., Git, local file system, database).
2. Applications fetch their configurations dynamically based on `spring.application.name` and `spring.profiles.active`.
3. Changes in configurations can be refreshed at runtime without restarting applications using `@RefreshScope`.

Spring Cloud Config simplifies configuration management for microservices, enabling flexibility and scalability in cloud-native applications.

## Running the Application
1. Start the application using:
   ```sh
   mvn spring-boot:run 
   ```
2. Monitor job execution logs for batch processing results.