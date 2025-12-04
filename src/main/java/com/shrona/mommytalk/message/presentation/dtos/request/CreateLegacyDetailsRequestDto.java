package com.shrona.mommytalk.message.presentation.dtos.request;

import jakarta.validation.constraints.NotNull;

public record CreateLegacyDetailsRequestDto(
    @NotNull(message = "Entitlement ID는 필수입니다")
    Long entitlementId
) {

}
