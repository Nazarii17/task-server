package com.ntj.dataprovider.controller;

import com.ntj.dataprovider.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/v1")
    public List<User> getUsesV1() {
        return getUsersV1();
    }

    @GetMapping("/v2")
    public List<User> getUserV2() {
        return getUsersV2();
    }

    private List<User> getUsersV1() {
        return List.of(
                new User("1", "John", "jonh@gmail.com"),
                new User("2", "Mary", "mary@gmail.com"),
                new User("3", "Jane", "jane@gmail.com")
        );
    }

    private List<User> getUsersV2() {
        return List.of(
                new User(UUID.randomUUID().toString(), "John", "jonh@gmail.com"),
                new User(UUID.randomUUID().toString(), "Mary", "mary@gmail.com"),
                new User(UUID.randomUUID().toString(), "Jane", "jane@gmail.com")
        );
    }
}
