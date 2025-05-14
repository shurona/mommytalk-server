package com.shrona.line_demo.line.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ReservationStatus {
    PREPARE("PREPARE"),
    FAIL("FAIL"),
    COMPLETE("COMPLETE");

    private final String role;

}
