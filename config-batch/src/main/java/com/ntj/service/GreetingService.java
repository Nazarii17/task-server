package com.ntj.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;


@Slf4j
@RefreshScope
@Service
public class GreetingService {

    @Value("${application.greeting.message}")
    public String greetingMessage;
    @Value("${application.environment.name}")
    public String environmentName;
    @Value("${application.prod-message:}")
    public String prodMessage;

    @EventListener(ApplicationReadyEvent.class)
    public String getGreetingMessage() {
        log.info(greetingMessage);
        log.info("Active profile: {}", environmentName);
        return greetingMessage;
    }

    @PostConstruct()
    @Profile("prod")
    public void init() {
        log.info(prodMessage);
    }
}
