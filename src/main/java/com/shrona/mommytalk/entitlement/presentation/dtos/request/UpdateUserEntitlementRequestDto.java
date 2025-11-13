package com.shrona.mommytalk.entitlement.presentation.dtos.request;

import com.shrona.mommytalk.entitlement.domain.EntitlementStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 유저 상품권 수정 요청 DTO (상태 변경 및 종료일 연장)
 */
public record UpdateUserEntitlementRequestDto(
    EntitlementStatus status,

    @NotNull(message = "종료일은 필수입니다")
    LocalDate endDate
) {
}
