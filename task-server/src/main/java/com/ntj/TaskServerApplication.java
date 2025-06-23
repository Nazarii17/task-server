package com.ntj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableJpaRepositories(basePackages = "com.ntj.repository.taskserver")
@EntityScan(basePackages = "com.ntj.model.taskserver")
@EnableScheduling
@SpringBootApplication
public class TaskServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskServerApplication.class, args);
    }
}