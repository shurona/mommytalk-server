package com.shrona.mommytalk.group.domain;

import lombok.Getter;

@Getter
public enum GroupType {

    CUSTOM("CUSTOM"),
    AUTO_ACTIVE("AUTO_ACTIVE"),
    AUTO_ENDED("AUTO_ENDED");
    private final String code;

    GroupType(String code) {
        this.code = code;
    }
}
