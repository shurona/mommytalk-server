package com.shrona.mommytalk.entitlement.presentation.dtos.response;

import java.time.LocalDateTime;

public record EntitlementResponseDto(
    String productName,
    LocalDateTime serviceStart,
    LocalDateTime serviceEnd,
    String status
) {

    public static EntitlementResponseDto from() {
        return new EntitlementResponseDto(
            "마미톡365", LocalDateTime.now(), LocalDateTime.now(), "active"
        );
    }

}
