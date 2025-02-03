package com.ntj.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ConfigServerHealthIndicator implements HealthIndicator {

    @Value("${application.config-server-health-url}")
    private String configServerHealthUrl;

    private final RestTemplate restTemplate;

    public ConfigServerHealthIndicator() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public Health health() {
        try {
            // Check if Config Server is up by hitting its /actuator/health endpoint
            restTemplate.getForObject(configServerHealthUrl, String.class);
            return Health.up().withDetail("Config Server", "Up and running").build();
        } catch (Exception e) {
            return Health.down().withDetail("Config Server", "Unavailable").build();
        }
    }
}

