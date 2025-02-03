package com.ntj.config.batch;

import com.ntj.config.ApplicationProperties;
import com.ntj.config.decider.JobDecider;
import com.ntj.config.listener.SimpleStepExecutionListener;
import com.ntj.constant.Status;
import com.ntj.domain.entity.AppConfigurationSnapshot;
import com.ntj.domain.record.AppConfigRecord;
import com.ntj.repository.AppConfigurationSnapshotRepository;
import com.ntj.service.TaskService;
import com.ntj.util.JobUtil;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class BatchConfig {

    private final ApplicationProperties applicationProperties;
    private final AppConfigurationSnapshotRepository appConfigurationSnapshotRepository;
    private final TaskService taskService;
    private final SimpleStepExecutionListener simpleStepExecutionListener;

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public Job configBatchJob(final JobRepository jobRepository,
                              final @Qualifier("collectConfigsDataFlow") Flow collectDataFlow,
                              final @Qualifier("configsNotChangedFlow") Flow configsNotChangedFlow,
                              final @Qualifier("configsChangedFlow") Flow configsChangedFlow,
                              final @Qualifier("incrementNumberJobExecutionListener") JobExecutionListener incrementNumberJobExecutionListener,
                              final JobDecider decider) {
        return new JobBuilder("configBatchJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(collectDataFlow)
                .next(decider)
                .on("CONFIGS_NOT_CHANGED").to(configsNotChangedFlow)
                .from(decider).on("CONFIGS_CHANGED").to(configsChangedFlow)
                .end()
                .listener(incrementNumberJobExecutionListener)
                .build();
    }

    @Bean
    public Flow collectConfigsDataFlow(final @Qualifier("getDataStep") Step getDataStep,
                                       final @Qualifier("resolveStatusesStep") Step resolveStatusesStep) {
        return new FlowBuilder<Flow>("collectConfigsDataFlow")
                .start(getDataStep)
                .next(resolveStatusesStep)
                .end();
    }

    @Bean
    public Step getDataStep(final JobRepository jobRepository,
                            final PlatformTransactionManager platformTransactionManager,
                            final ItemReader<AppConfigRecord> itemReader,
                            final ItemProcessor<AppConfigRecord, AppConfigurationSnapshot> itemProcessor,
                            final ItemWriter<AppConfigurationSnapshot> itemWriter,
                            final @Qualifier("incrementNumberStepExecutionListener") StepExecutionListener incrementNumberStepExecutionListener) {
        return new StepBuilder("getDataStep", jobRepository)
                .<AppConfigRecord, AppConfigurationSnapshot>chunk(2, platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .allowStartIfComplete(true)
                .listener(incrementNumberStepExecutionListener)
                .build();
    }

    @Bean
    public Step resolveStatusesStep(final JobRepository jobRepository,
                                    final PlatformTransactionManager platformTransactionManager,
                                    final @Qualifier("incrementNumberStepExecutionListener") StepExecutionListener incrementNumberStepExecutionListener) {
        return new StepBuilder("resolveStatusesStep", jobRepository)
                .tasklet((stepContribution, chunkContext) -> {

                    final Collection<String> appNames = applicationProperties.getAppConfigs().keySet();

                    final Map<String, String> appStatus = new HashMap<>();

                    for (String appName : appNames) {
                        log.info("Resolve configs for Application: {}", appName);
                        final AppConfigurationSnapshot currentAppConfigSnapshot = appConfigurationSnapshotRepository
                                .findLastAppConfigurationSnapshot(appName)
                                .orElse(null);

                        log.info("Current AppConfigSnapshot: {}, appName: {}", currentAppConfigSnapshot, appName);

                        final AppConfigurationSnapshot lastAppConfigurationSnapshot = appConfigurationSnapshotRepository
                                .findOneOfLastExecutionAppConfigurationSnapshot(appName, taskService.getCurrentTaskExecutionId()).
                                orElse(null);

                        log.info("Last AppConfigSnapshot: {}, appName: {}", lastAppConfigurationSnapshot, appName);

                        final String currentPropertySources = currentAppConfigSnapshot != null
                                ? currentAppConfigSnapshot.getPropertySources()
                                : null;
                        final String lastPropertySources = lastAppConfigurationSnapshot != null
                                ? lastAppConfigurationSnapshot.getPropertySources()
                                : null;

                        final Status status = JobUtil.getStatus(currentPropertySources, lastPropertySources, lastAppConfigurationSnapshot);
                        appStatus.put(appName, status.getValue());
                    }
                    final Collection<String> statuses = appStatus.values();
                    final ExecutionContext executionContext = chunkContext.getStepContext()
                            .getStepExecution()
                            .getJobExecution()
                            .getExecutionContext();

                    JobUtil.putStatusToExecutionContext(statuses, executionContext);
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)

                .listener(simpleStepExecutionListener)
                .listener(incrementNumberStepExecutionListener)
                .allowStartIfComplete(true)

                .build();
    }
}
