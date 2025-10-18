package com.shrona.mommytalk.message.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AudioRole {
    MOMMY("MOMMY"),
    CHILD("CHILD"),
    ;

    private final String role;
}
