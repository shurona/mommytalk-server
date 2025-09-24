package com.shrona.mommytalk.line.infrastructure.sender.dto.flex;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ContentType {
    TEXT("text"),
    BOX("box"),
    BUTTON("button"),
    BUBBLE("bubble"),
    FLEX("flex"),
    ;

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
