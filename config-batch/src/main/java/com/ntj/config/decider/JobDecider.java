package com.ntj.config.decider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(final JobExecution jobExecution,
                                      final StepExecution stepExecution) {

        final ExecutionContext executionContext = jobExecution.getExecutionContext();
        final boolean isAppsChanged = (boolean) executionContext.get("isAppsChanged");
        final String simpleStepStatus = (String) executionContext.get("simpleStepStatus");

        log.info("In JobDecider isAppsChanged: {}, simpleStepStatus: {}", isAppsChanged, simpleStepStatus);
        return new FlowExecutionStatus(simpleStepStatus);
    }
}
