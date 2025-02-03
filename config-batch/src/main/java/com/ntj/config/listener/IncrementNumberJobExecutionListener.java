package com.ntj.config.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IncrementNumberJobExecutionListener implements JobExecutionListener {

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        jobExecution.getExecutionContext().put("incrementNumber", 0);
        log.info("\uD83D\uDD22 Before Job Execution incrementNumber: 0");
    }

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        final Integer incrementNumber = (Integer) jobExecution.getExecutionContext().get("incrementNumber");
        log.info("\uD83D\uDD22 After job incrementNumber: {}", incrementNumber);
    }
}
