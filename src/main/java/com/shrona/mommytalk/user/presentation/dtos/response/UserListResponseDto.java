package com.shrona.mommytalk.user.presentation.dtos.response;

import com.shrona.mommytalk.user.infrastructure.dao.UserListProjection;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record UserListResponseDto(
    Long userId,
    String email,
    String name,
    String phoneNumber,
    LocalDateTime signupAt,
    LocalDateTime lastestPurchaseAt,
    String lastestProductName,
    String socialId,
    Integer userLevel,
    Integer childLevel,
    String childName,
    Boolean channelFriend,
    Integer purchaseCount
) {

    /**
     * Projection을 responseDto로 변환
     */
    public static UserListResponseDto from(UserListProjection listProjection) {
        return UserListResponseDto.builder()
            .userId(listProjection.userId())
            .email(listProjection.email())
            .name(listProjection.name())
            .phoneNumber(listProjection.phoneNumber().getPhoneNumber())
            .signupAt(listProjection.signupAt())
            .lastestPurchaseAt(listProjection.lastestPurchaseAt())
            .lastestProductName(listProjection.lastProductName())
            .socialId(listProjection.socialId())
            .userLevel(listProjection.userLevel())
            .childLevel(listProjection.childLevel())
            .childName(listProjection.childName())
            .channelFriend(listProjection.channelFriend())
            .purchaseCount(listProjection.purchaseCount())
            .build();
    }

}
