package com.ntj.config.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class IncrementNumberStepExecutionListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        final Integer incrementNumber = (Integer) stepExecution.getJobExecution()
                .getExecutionContext()
                .get("incrementNumber");
        if (incrementNumber == null) {
            stepExecution.getJobExecution()
                    .getExecutionContext().put("incrementNumber", 0);
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        final Integer incrementNumber = (Integer) stepExecution.getJobExecution()
                .getExecutionContext()
                .get("incrementNumber");
        stepExecution.getJobExecution()
                .getExecutionContext().put("incrementNumber", incrementNumber + 1);
        return null;
    }
}
