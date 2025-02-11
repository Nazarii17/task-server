package com.ntj.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    private Environment environment;
    private Map<String, String> appConfigs;

    @Setter
    @Getter
    public static class Environment {
        private String name;

    }
}
