package com.shrona.mommytalk.line.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ReservationStatus {
    PREPARE("PREPARE"),
    FAIL("FAIL"),
    COMPLETE("COMPLETE"),
    CANCEL("CANCEL");

    private final String status;

}
