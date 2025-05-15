package com.shrona.line_demo.line.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum EventType {
    MESSAGE("MESSAGE"),
    FOLLOW("FOLLOW"),
    UNFOLLOW("UNFOLLOW");

    private final String type;
}
