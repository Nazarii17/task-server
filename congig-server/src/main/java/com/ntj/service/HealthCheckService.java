package com.ntj.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HealthCheckService {

    @Value("${application.greeting.message}")
    public String greetingMessage;

    @EventListener(ApplicationReadyEvent.class)
    public void getGreetingMessage() {
        log.info(greetingMessage);
    }
}
