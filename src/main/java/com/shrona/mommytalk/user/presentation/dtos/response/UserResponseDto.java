package com.shrona.mommytalk.user.presentation.dtos.response;

import com.shrona.mommytalk.entitlement.presentation.dtos.response.EntitlementResponseDto;
import com.shrona.mommytalk.user.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record UserResponseDto(
    Long userId,
    String email,
    String name,
    String phoneNumber,
    LocalDateTime signupAt,
    LocalDateTime latestPurchaseAt,
    String latestProductName,
    Integer totalAmount,
    String socialId,
    Integer userLevel,
    Integer childLevel,
    String childName,
    String channelFriend,
    Integer purchaseCount,

    List<EntitlementResponseDto> entitlements
) {

    public static UserResponseDto from(User user, String socialId, List<com.shrona.mommytalk.entitlement.domain.UserEntitlement> userEntitlements) {
        // UserEntitlement 목록을 EntitlementResponseDto로 변환
        List<EntitlementResponseDto> entitlementDtos = userEntitlements.stream()
            .map(EntitlementResponseDto::from)
            .toList();

        return UserResponseDto.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .phoneNumber(user.getPhoneNumber().getPhoneNumber())
            .signupAt(user.getCreatedAt())
            .latestPurchaseAt(LocalDateTime.now())
            .latestProductName("")
            .totalAmount(0)
            .socialId(socialId)
            .userLevel(user.getUserLevel())
            .childLevel(user.getChildLevel())
            .childName(user.getChildName())
            .entitlements(entitlementDtos)
            .build();
    }

}
