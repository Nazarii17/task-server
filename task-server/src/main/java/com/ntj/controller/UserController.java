package com.ntj.controller;

import com.ntj.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@RefreshScope
@RestController
@RequestMapping("api/user")
public class UserController {

    @Value("${application.data-provider-url}")
    private String dataProviderUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public List<User> getUsers() {
        log.info("retrieving users from: {}", dataProviderUrl);

        final ResponseEntity<List<User>> response = restTemplate
                .exchange(dataProviderUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }
}
