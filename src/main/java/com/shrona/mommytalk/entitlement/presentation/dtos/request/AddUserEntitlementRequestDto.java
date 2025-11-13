package com.shrona.mommytalk.entitlement.presentation.dtos.request;

import com.shrona.mommytalk.entitlement.domain.EntitlementStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 유저 상품권 추가 요청 DTO
 */
public record AddUserEntitlementRequestDto(
    @NotNull(message = "유저 ID는 필수입니다")
    Long userId,

    @NotNull(message = "채널 ID는 필수입니다")
    Long channelId,

    @NotNull(message = "상품 ID는 필수입니다")
    Long entitlementId,

    @NotNull(message = "상태는 필수입니다")
    EntitlementStatus status,

    @NotNull(message = "시작일은 필수입니다")
    LocalDate startDate,

    @NotNull(message = "종료일은 필수입니다")
    LocalDate endDate
) {
}
