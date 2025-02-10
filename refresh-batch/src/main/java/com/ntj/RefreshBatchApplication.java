package com.ntj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

@EnableTask
@SpringBootApplication
public class RefreshBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(RefreshBatchApplication.class, args);
    }
}
