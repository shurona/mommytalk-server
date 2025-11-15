package com.shrona.mommytalk.entitlement.presentation.dtos.response;

import com.shrona.mommytalk.entitlement.domain.UserEntitlement;
import java.time.LocalDate;

public record EntitlementResponseDto(
    Long userEntitlementId,
    Long entitlementId,
    String entitlementName,
    LocalDate serviceStart,
    LocalDate serviceEnd,
    String status
) {

    public static EntitlementResponseDto from(UserEntitlement userEntitlement) {
        return new EntitlementResponseDto(
            userEntitlement.getId(),
            userEntitlement.getEntitlement().getId(),
            userEntitlement.getEntitlement().getName(),
            userEntitlement.getStartDate(),
            userEntitlement.getEndDate(),
            userEntitlement.getStatus().name().toLowerCase()
        );
    }

}
