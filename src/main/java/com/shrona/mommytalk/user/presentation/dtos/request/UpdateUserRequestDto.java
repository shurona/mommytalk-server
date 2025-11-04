package com.shrona.mommytalk.user.presentation.dtos.request;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record UpdateUserRequestDto(
    String childName,
    String phoneNumber,
    Integer userLevel,
    Integer childLevel
) {

    public static UpdateUserRequestDto of(String childName, Integer userLevel, Integer childLevel) {
        return UpdateUserRequestDto.builder()
            .childName(childName)
            .userLevel(userLevel)
            .childLevel(childLevel)
            .build();
    }

}
