package com.ntj.config.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class TaskletJobConfig {

    @Bean
    public Flow configsNotChangedFlow(final @Qualifier("taskletStep1") Step taskletStep1,
                                      final @Qualifier("taskletStep2") Step taskletStep2,
                                      final @Qualifier("taskletStep3") Step taskletStep3) {
        log.info("Executing flow according to CONFIGS_NOT_CHANGED");
        return new FlowBuilder<Flow>("taskletFlow1")
                .start(taskletStep1)
                .next(taskletStep2)
                .next(taskletStep3)
                .build();
    }

    @Bean
    public Flow configsChangedFlow(final Step taskletStep4,
                                   final Step taskletStep5,
                                   final Step taskletStep6) {
        log.info("Executing flow according to CONFIGS_CHANGED");
        return new FlowBuilder<Flow>("taskletFlow2")
                .start(taskletStep4)
                .next(taskletStep5)
                .next(taskletStep6)
                .build();
    }

    @Bean
    public Step taskletStep1(final JobRepository jobRepository,
                             final PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("taskletStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Executing tasklet CONFIGS_NOT_CHANGED step 1...");
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)

                .allowStartIfComplete(true)

                .build();
    }

    @Bean
    public Step taskletStep2(final JobRepository jobRepository,
                             final PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("taskletStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Executing tasklet CONFIGS_NOT_CHANGED step 2...");
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)

                .allowStartIfComplete(true)

                .build();
    }

    @Bean
    public Step taskletStep3(final JobRepository jobRepository,
                             final PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("taskletStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Executing tasklet CONFIGS_NOT_CHANGED step 3...");
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)

                .allowStartIfComplete(true)

                .build();
    }

    @Bean
    public Step taskletStep4(final JobRepository jobRepository,
                             final PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("taskletStep4", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Executing tasklet CONFIGS_CHANGED step 4...");
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager).build();
    }

    @Bean
    public Step taskletStep5(final JobRepository jobRepository,
                             final PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("taskletStep5", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Executing tasklet CONFIGS_CHANGED step 5...");
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)

                .allowStartIfComplete(true)

                .build();
    }

    @Bean
    public Step taskletStep6(final JobRepository jobRepository,
                             final PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("taskletStep6", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Executing tasklet CONFIGS_CHANGED step 6...");
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)

                .allowStartIfComplete(true)

                .build();
    }
}
