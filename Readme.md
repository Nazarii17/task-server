# Task Server Setup Guide

## Overview
This guide provides the setup instructions for running the `task-server` along with its dependencies, including a MySQL database, `config-server`, and batch job applications (`config-batch` and `refresh-batch`).

## Prerequisites
- Docker & Docker Compose installed
- Java 17+
- Maven installed
- Postman (optional, for API testing)

## Database Setup

### Docker Compose Configuration
To set up the MySQL database, use the following `docker-compose.yml`:

```yaml
docker compose
version: '3.8'

services:
  mysql:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: task_server_db
    environment:
      MYSQL_DATABASE: task_server_schema
      MYSQL_USER: batch_user
      MYSQL_PASSWORD: batchuser
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3307:3306"
    volumes:
      - /Users/nazarii.tkachuk/Development/App_volumes/task_server_volume:/var/lib/mysql
    image: task-server-db:latest
```

### MySQL Dockerfile

```dockerfile
# Use the official MySQL image from Docker Hub
FROM mysql:8.0

# Set environment variables
ENV MYSQL_DATABASE=task_server_schema \
    MYSQL_USER=batch_user \
    MYSQL_PASSWORD=batchuser \
    MYSQL_ROOT_PASSWORD=root

# Expose the default MySQL port
EXPOSE 3306
```

### Running the Database
To start the MySQL database, run:
```sh
  docker-compose up -d
```

## Building the Applications

Compile and package all projects:
```sh
  mvn clean install
```

## Running the Services

### Start Config Server
```sh
  mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Start Task Server
```sh
  mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Start Config Batch
```sh
  mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Start Refresh Batch
```sh
  mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Task Server API

### Get All Tasks
```sh
  curl --location 'http://localhost:8080/tasks'
```

### Launch a Task
Run `refresh-batch`:
```sh
  curl --location 'http://localhost:8080/tasks/refresh-batch' \
--header 'Content-Type: application/json' \
--data '{}'
```

Run `config-batch`:
```sh
  curl --location 'http://localhost:8080/tasks/config-batch' \
--header 'Content-Type: application/json' \
--data '{}'
```

## Adding Cron Schedulers
You can add scheduled task executions via SQL:

```sql
INSERT INTO task_server_schema.cron_job (job_name, cron_expression) VALUES ('config-batch', '0 0/1 * * * ?');
INSERT INTO task_server_schema.cron_job (job_name, cron_expression) VALUES ('refresh-batch', '0 0/2 * * * ?');
```

## Connecting to MySQL
Use the following credentials to connect to the database:
- JDBC URL: `jdbc:mysql://localhost:3306/task_server_schema`
- User: `batch_user`
- Password: `batchuser`

## Checking Execution Data
Check the following tables for task execution details:
- `TASK_EXECUTION`
- `BATCH_JOB_EXECUTION`
- `BATCH_STEP_EXECUTION`

## Conclusion
This guide provides all necessary steps to set up and run the `task-server` with dependent services. Use Postman or cURL for API testing, and check the database for execution details.

