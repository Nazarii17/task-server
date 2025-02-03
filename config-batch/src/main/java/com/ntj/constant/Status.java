package com.ntj.constant;

import lombok.Getter;

@Getter
public enum Status {
    CONFIGS_NOT_CHANGED("CONFIGS_NOT_CHANGED"),
    CONFIGS_CHANGED("CONFIGS_CHANGED");

    Status(String value) {
        this.value = value;
    }

    private final String value;
}
