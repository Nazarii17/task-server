package com.ntj.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
