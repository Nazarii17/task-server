package com.ntj.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConfigServerHealthCheck {

    @Bean
    public ApplicationRunner checkConfigServer() {
        return args -> {
            String configServerUrl = "http://localhost:8888/actuator/health";
            RestTemplate restTemplate = new RestTemplate();

            // Create a retry template with custom retry policy and backoff policy
            RetryTemplate retryTemplate = new RetryTemplate();

            // Set the retry policy to allow a max of 5 attempts
            retryTemplate.setRetryPolicy(new SimpleRetryPolicy(5));

            // Set the backoff policy with a fixed interval and multiplier
            FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
            backOffPolicy.setBackOffPeriod(2000); // 2 seconds between retries
            retryTemplate.setBackOffPolicy(backOffPolicy);

            // Execute the request with retry logic
            try {
                retryTemplate.execute(context -> {
                    // Log the retry attempt
                    System.out.println("Attempting to connect to Config Server (Attempt #" + (context.getRetryCount() + 1) + ")");
                    restTemplate.getForObject(configServerUrl, String.class);
                    return null; // No result is needed, we just want to check connectivity
                });
                System.out.println("Config Server is up!");
            } catch (Exception e) {
                System.err.println("Config Server is not available after retries. Exiting...");
                System.exit(1);
            }
        };
    }
}