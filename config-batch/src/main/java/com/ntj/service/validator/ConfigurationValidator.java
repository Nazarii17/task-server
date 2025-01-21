package com.ntj.service.validator;

import com.ntj.config.ApplicationProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ConfigurationValidator {

    private final ApplicationProperties applicationProperties;

    public ConfigurationValidator(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @PostConstruct
    public void validate() {
        Map<String, String> appConfigs = applicationProperties.getAppConfigs();

        if (appConfigs == null || appConfigs.isEmpty()) {
            throw new IllegalStateException("No app configurations found in application.yml under 'appConfigs'.");
        }
        log.info("Loaded app configurations: {}", appConfigs);
    }
}

