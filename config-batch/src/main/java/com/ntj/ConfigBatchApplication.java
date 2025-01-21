package com.ntj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

@EnableTask
@SpringBootApplication
public class ConfigBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigBatchApplication.class, args);
    }
}
