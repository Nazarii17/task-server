package com.ntj.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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

    @EventListener(ApplicationReadyEvent.class)
    public String getGreetingMessage() {
        log.info(greetingMessage);
        log.info("Active profile: {}", environmentName);
        return greetingMessage;
    }
}
