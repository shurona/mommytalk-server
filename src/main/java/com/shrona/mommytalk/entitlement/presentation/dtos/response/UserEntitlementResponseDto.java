package com.shrona.mommytalk.entitlement.presentation.dtos.response;

import com.shrona.mommytalk.entitlement.domain.EntitlementStatus;
import java.time.LocalDate;
import lombok.Builder;

/**
 * 유저 상품권 응답 DTO
 */
@Builder
public record UserEntitlementResponseDto(
    Long userId,
    Long userGroupId,
    Long groupId,
    String entitlementName,
    EntitlementStatus status,
    LocalDate startDate,
    LocalDate endDate
) {

    public static UserEntitlementResponseDto of(
        Long userId,
        Long userGroupId,
        Long groupId,
        String entitlementName,
        EntitlementStatus status,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return UserEntitlementResponseDto.builder()
            .userId(userId)
            .userGroupId(userGroupId)
            .groupId(groupId)
            .entitlementName(entitlementName)
            .status(status)
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }
}
