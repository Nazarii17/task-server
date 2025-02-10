package com.ntj.config;

import com.ntj.listener.TaskServerRefreshJobListener;
import com.ntj.service.RefreshMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class BatchJobConfig {

    private static final Logger logger = LoggerFactory.getLogger(BatchJobConfig.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final BatchProperties batchProperties;
    private final RefreshMessageService refreshMessageService;

    private final int REFRESH_INTERVAL = 60;
    private final int RETRY_COUNT = 3;

    @Bean
    public Job refreshJob(final JobRepository jobRepository,
                          final @Qualifier("taskServerRefresh") Step taskServerRefresh,
                          final @Qualifier("refreshBatchRefresh") Step refreshBatchRefresh,
                          final @Qualifier("configBatchRefresh") Step configBatchRefresh,
                          final @Qualifier("printRefreshableMessageOnInit") Step printRefreshableMessageOnInit,
                          final @Qualifier("printRefreshableMessageOnEnd") Step printRefreshableMessageOnEnd) {
        return new JobBuilder("refreshJob", jobRepository)
                .listener(new TaskServerRefreshJobListener())
                .start(printRefreshableMessageOnInit)
                .split(new SimpleAsyncTaskExecutor())
                .add(new FlowBuilder<Flow>("parallelStepsFlow")
                        .start(taskServerRefresh)
                        .next(refreshBatchRefresh)
                        .next(configBatchRefresh)
                        .build())
                .next(printRefreshableMessageOnEnd)
                .end()
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step taskServerRefresh(final JobRepository jobRepository,
                                  final PlatformTransactionManager platformTransactionManager) {
        return createStep(jobRepository, platformTransactionManager, "trigger task-server actuator refresh", "task-server");
    }

    @Bean
    public Step refreshBatchRefresh(final JobRepository jobRepository,
                                    final PlatformTransactionManager platformTransactionManager) {
        return createStep(jobRepository, platformTransactionManager, "trigger refresh-batch actuator refresh", "refresh-batch");
    }

    @Bean
    public Step configBatchRefresh(final JobRepository jobRepository,
                                   final PlatformTransactionManager platformTransactionManager) {
        return createStep(jobRepository, platformTransactionManager, "trigger config-batch actuator refresh", "config-batch");
    }

    @Bean
    public Step printRefreshableMessageOnInit(final JobRepository jobRepository,
                                              final PlatformTransactionManager platformTransactionManager) {
        return buildTaskletStep("✉\uFE0F print message on job init", jobRepository, "-BEFORE-", platformTransactionManager);
    }

    @Bean
    public Step printRefreshableMessageOnEnd(final JobRepository jobRepository,
                                             final PlatformTransactionManager platformTransactionManager) {
        return buildTaskletStep("✉\uFE0F print message on job end", jobRepository, "-AFTER-", platformTransactionManager);
    }

    private Step createStep(final JobRepository jobRepository,
                            final PlatformTransactionManager platformTransactionManager,
                            final String stepName,
                            final String appName) {
        return new StepBuilder(stepName, jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    final String url = batchProperties.getUrls().get(appName);

                    if (url != null) {
                        for (int i = 0; i < RETRY_COUNT; i++) {
                            try {
                                HttpHeaders headers = new HttpHeaders();
                                headers.setContentType(MediaType.APPLICATION_JSON);
                                HttpEntity<String> entity = new HttpEntity<>("{}", headers);

                                restTemplate.postForObject(url, entity, String.class);
                                logger.info("Successfully called [{}] -> {}", appName, url);
                            } catch (HttpClientErrorException e) {
                                logger.error("HTTP error when calling [{}] -> {}: {}", appName, url, e.getMessage());
                            } catch (ResourceAccessException e) {
                                logger.error("Service unavailable when calling [{}] -> {}: {}", appName, url, e.getMessage());
                            } catch (Exception e) {
                                logger.error("Unexpected error when calling [{}] -> {}: {}", appName, url, e.getMessage());
                            }
                            TimeUnit.SECONDS.sleep(REFRESH_INTERVAL);
                        }
                    } else {
                        logger.warn("No URL found for app [{}]", appName);
                    }
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    private TaskletStep buildTaskletStep(final String stepName,
                                         final JobRepository jobRepository,
                                         final String phase,
                                         final PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder(stepName, jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    refreshMessageService.printRefreshableMessage(phase);
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)
                .allowStartIfComplete(true)
                .build();
    }
}
