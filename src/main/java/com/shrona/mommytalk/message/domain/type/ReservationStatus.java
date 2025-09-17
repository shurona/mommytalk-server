package com.shrona.mommytalk.message.domain.type;

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
