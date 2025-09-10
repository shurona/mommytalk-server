package com.shrona.mommytalk.user.infrastructure.dao;

import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import java.time.LocalDateTime;

public record UserListProjection(
    Long userId,
    String email,
    String name,
    PhoneNumber phoneNumber,
    LocalDateTime signupAt,
    LocalDateTime lastestPurchaseAt,
    String lastProductName,
    String socialId,
    Integer userLevel,
    Integer childLevel,
    String childName,
    Boolean channelFriend,
    Integer purchaseCount
) {

}
