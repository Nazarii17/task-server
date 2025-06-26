package com.ntj.dataprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class DataProviderApplication {

    private final Logger log = LoggerFactory.getLogger(DataProviderApplication.class);

    @Value("${test.message:DEFAULT MESSAGE}")
    private String message;

    @Value("${display.test.message:true}")
    private boolean displayTestMessage;

    public static void main(String[] args) {
        SpringApplication.run(DataProviderApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (displayTestMessage) {
            log.info("< - - - DataProviderApplication init with message: {} - - - >", message);
        } else {
            log.info("< - - - DataProviderApplication init without message - - - >");
        }
    }
}
