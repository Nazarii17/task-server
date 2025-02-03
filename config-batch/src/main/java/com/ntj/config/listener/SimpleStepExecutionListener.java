package com.ntj.config.listener;

import com.ntj.domain.entity.AppConfigurationSnapshot;
import com.ntj.repository.AppConfigurationSnapshotRepository;
import com.ntj.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class SimpleStepExecutionListener implements StepExecutionListener {

    private final AppConfigurationSnapshotRepository appConfigurationSnapshotRepository;
    private final TaskService taskService;
    private final ApplicationArguments args;
    private final Environment environment;

    @Override
    @Transactional
    @AfterStep
    public ExitStatus afterStep(final StepExecution stepExecution) {

        final boolean isAppsChanged = (boolean) stepExecution.getJobExecution().getExecutionContext().get("isAppsChanged");

        final Long currentTaskExecutionId = taskService.getCurrentTaskExecutionId();
        final List<AppConfigurationSnapshot> appConfigurationSnapshots = appConfigurationSnapshotRepository
                .findAllByTaskId(currentTaskExecutionId);
        final String source = getSource();

        appConfigurationSnapshots.forEach(appConfigurationSnapshot -> {
            final String status = isAppsChanged ? "CHANGED" : "SAME_AS_BEFORE";
            appConfigurationSnapshot.setStatus(status);

            appConfigurationSnapshot.setSource(source);
        });

        return stepExecution.getExitStatus();
    }

    private String getSource() {
        final String source = args.getNonOptionArgs().stream()
                .filter(s -> s.contains("source="))
                .findFirst().map(s -> s.replace("source=", ""))
                .orElseGet(() -> environment.getProperty("source", "CONFIG_BATCH_APPLICATION"));

        log.info("Task source: {}", source);
        return source;
    }
}
