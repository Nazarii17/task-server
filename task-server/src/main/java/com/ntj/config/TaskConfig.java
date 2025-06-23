package com.ntj.config;

import org.springframework.cloud.task.configuration.DefaultTaskConfigurer;
import org.springframework.cloud.task.configuration.TaskConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class TaskConfig {

    private final DataSource dataSource;

    public TaskConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public TaskConfigurer taskConfigurer() {
        return new DefaultTaskConfigurer(dataSource);
    }
}

