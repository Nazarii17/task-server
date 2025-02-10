package com.ntj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(PlatformTransactionManager transactionManager) {
        return transactionManager;
    }
}

