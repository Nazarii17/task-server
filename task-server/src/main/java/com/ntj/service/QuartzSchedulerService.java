package com.ntj.service;

import com.ntj.model.taskserver.CronJob;
import com.ntj.repository.taskserver.CronJobRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuartzSchedulerService {

    private final Scheduler scheduler;
    private final CronJobRepository cronJobRepository;

    @Scheduled(fixedDelay = 60000) // Runs every 60 seconds to refresh cron jobs
    @PostConstruct
    public void refreshScheduledJobs() {
        log.info("🔄 Checking for updated cron jobs...");
        List<CronJob> jobs = cronJobRepository.findAll();

        for (CronJob job : jobs) {
            scheduleOrUpdateJob(job);
        }
    }

    private void scheduleOrUpdateJob(final CronJob job) {
        try {
            final JobKey jobKey = new JobKey(job.getJobName(), "TASK_SERVER_TASKS_GROUP");

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey); // Remove old job if it exists
            }

            final JobDetail jobDetail = JobBuilder.newJob(QuartzTaskRunner.class)
                    .withIdentity(jobKey)
                    .usingJobData("jobName", job.getJobName())
                    .build();

            final CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(job.getJobName() + "_trigger", "TASK_SERVER_TASKS_GROUP")
                    .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("✅ Scheduled job: '{}' with cron: '{}'", job.getJobName(), job.getCronExpression());

        } catch (SchedulerException e) {
            log.error("❌ Failed to schedule job: {}", job.getJobName(), e);
        }
    }
}

