package com.ntj.config.batch;

import com.ntj.config.ApplicationProperties;
import com.ntj.config.SimpleStepExecutionListener;
import com.ntj.config.decider.JobDecider;
import com.ntj.domain.entity.AppConfigurationSnapshot;
import com.ntj.domain.record.AppConfigRecord;
import com.ntj.repository.AppConfigurationSnapshotRepository;
import com.ntj.service.TaskService;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
import java.util.Objects;

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
    public Job jobWithDecider(final JobRepository jobRepository,
                              final @Qualifier("chunkBasedFlow") Flow chunkBasedFlow,
                              final @Qualifier("configsNotChangedFlow") Flow configsNotChangedFlow,
                              final @Qualifier("configsChangedFlow") Flow configsChangedFlow,
                              final JobDecider decider) {
        return new JobBuilder("jobWithDecider", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkBasedFlow)
                .next(decider)
                .on("CONFIGS_NOT_CHANGED").to(configsNotChangedFlow)
                .from(decider).on("CONFIGS_CHANGED").to(configsChangedFlow)
                .end()
                .build();
    }

    @Bean
    public Flow chunkBasedFlow(final @Qualifier("chunkStep") Step chunkStep,
                               final @Qualifier("simpleStep") Step simpleStep) {
        return new FlowBuilder<Flow>("chunkBasedFlow")
                .start(chunkStep)
                .next(simpleStep)
                .end();
    }

    @Bean
    public Step chunkStep(final JobRepository jobRepository,
                          final PlatformTransactionManager platformTransactionManager,
                          final ItemReader<AppConfigRecord> itemReader,
                          final ItemProcessor<AppConfigRecord, AppConfigurationSnapshot> itemProcessor,
                          final ItemWriter<AppConfigurationSnapshot> itemWriter) {
        return new StepBuilder("chunkStep1", jobRepository)
                .<AppConfigRecord, AppConfigurationSnapshot>chunk(2, platformTransactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step simpleStep(final JobRepository jobRepository,
                           final PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("simpleStep", jobRepository)
                .tasklet((stepContribution, chunkContext) -> {

                    final Collection<String> appNames = applicationProperties.getAppConfigs().keySet();

                    final Map<String, String> appStatus = new HashMap<>();

                    for (String appName : appNames) {
                        System.out.println(appName);
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

                        final String status;
                        if (Objects.nonNull(currentPropertySources) && Objects.nonNull(lastPropertySources)) {
                            // Both are non-null, so compare them
                            if (currentPropertySources.equals(lastPropertySources)) {
                                status = "CONFIGS_NOT_CHANGED";
                            } else {
                                status = "CONFIGS_CHANGED";
                            }
                        } else if (Objects.isNull(lastAppConfigurationSnapshot) || Objects.isNull(lastPropertySources)) {
                            // Either lastAppConfigurationSnapshot or lastPropertySources is null
                            status = "CONFIGS_NOT_CHANGED";
                        } else {
                            // Fallback: If neither condition was met, assume no change
                            status = "CONFIGS_NOT_CHANGED";
                        }
                        appStatus.put(appName, status);
                    }
                    final Collection<String> statuses = appStatus.values();
                    final ExecutionContext executionContext = chunkContext.getStepContext()
                            .getStepExecution()
                            .getJobExecution()
                            .getExecutionContext();

                    if (statuses.contains("CONFIGS_CHANGED")) {
                        executionContext
                                .put("isAppsChanged", true);
                        executionContext.put("simpleStepStatus", "CONFIGS_CHANGED");
                    } else {
                        executionContext
                                .put("isAppsChanged", false);
                        executionContext.put("simpleStepStatus", "CONFIGS_NOT_CHANGED");
                    }
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)

                .listener(simpleStepExecutionListener)
                .allowStartIfComplete(true)

                .build();
    }
}
