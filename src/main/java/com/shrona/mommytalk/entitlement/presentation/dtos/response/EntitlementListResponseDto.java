package com.shrona.mommytalk.entitlement.presentation.dtos.response;

import com.shrona.mommytalk.entitlement.domain.Entitlement;
import com.shrona.mommytalk.entitlement.domain.EntitlementType;

public record EntitlementListResponseDto(
    Long id,
    String name,
    EntitlementType type
) {
    public static EntitlementListResponseDto from(Entitlement entitlement) {
        return new EntitlementListResponseDto(
            entitlement.getId(),
            entitlement.getName(),
            entitlement.getType()
        );
    }
}
