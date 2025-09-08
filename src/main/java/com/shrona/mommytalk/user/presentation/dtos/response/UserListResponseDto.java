package com.shrona.mommytalk.user.presentation.dtos.response;

import java.time.LocalDateTime;

public record UserListResponseDto(
    Long userId,
    String email,
    String name,
    String phoneNumber,
    LocalDateTime signupAt,
    LocalDateTime lastestPurchaseAt,
    String lastProductName,
    String lineId,
    String kakaoId,
    Long userLevel,
    Long childLevel,
    String childName,
    Boolean channelFriend,
    Integer purchaseCount
) {

}
