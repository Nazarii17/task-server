package com.ntj.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Slf4j
@RefreshScope
@Service
public class RefreshMessageService {

    @Value("${application.refresh-message}")
    private String message;

    public void printRefreshableMessage(final String phase) {
        try {
            log.info("- - - \uD83D\uDD04 Refresh message {} contest refresh: '{}'", phase, message);
        } catch (Exception e) {
            log.error("Failed to fetch message: {}", e.getMessage());
        }
    }
}
