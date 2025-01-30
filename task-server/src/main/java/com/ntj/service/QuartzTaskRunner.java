package com.ntj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class QuartzTaskRunner implements Job {

    private final TaskLauncherService taskLauncherService;

    @Override
    public void execute(final JobExecutionContext context) {
        final String jobName = context.getJobDetail()
                .getJobDataMap()
                .getString("jobName");
        log.info("🚀 Running Quartz Job: {}", jobName);
        taskLauncherService.launchTask(jobName, Map.of("source", "QUARTZ_TASK_RUNNER"));
    }
}
