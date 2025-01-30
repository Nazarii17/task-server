package com.ntj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TaskServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskServerApplication.class, args);
    }
}