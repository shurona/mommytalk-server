package com.shrona.mommytalk.entitlement.presentation.dtos.request;

import com.shrona.mommytalk.entitlement.domain.EntitlementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 상품 생성 요청 DTO
 */
public record CreateEntitlementRequestDto(
    @NotBlank(message = "상품명은 필수입니다")
    String name,

    @NotNull(message = "상품 타입은 필수입니다")
    EntitlementType type
) {
}
