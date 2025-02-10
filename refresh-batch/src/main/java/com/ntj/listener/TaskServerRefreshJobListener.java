package com.ntj.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class TaskServerRefreshJobListener implements JobExecutionListener {

    private final RestTemplate restTemplate = new RestTemplate();
    private final static String TASK_SERVER_MESSAGE_URL = "http://localhost:8080/refresh/message";

    @Override
    public void beforeJob(final JobExecution jobExecution) {
        printMessage("Before");
    }

    @Override
    public void afterJob(final JobExecution jobExecution) {
        printMessage("After");
    }

    private void printMessage(final String phase) {
        try {
            final String response = restTemplate.getForObject(TASK_SERVER_MESSAGE_URL, String.class);
            log.info("- - - {} Job Execution -> Task Server message: '{}'", phase, response);
        } catch (Exception e) {
            log.error("Failed to fetch message: {}", e.getMessage());
        }
    }
}

