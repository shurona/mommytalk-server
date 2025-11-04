package com.shrona.mommytalk.auth.presentation.dtos.response;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record UserAuthResponseDto(
    String token,
    LineAuthUser user
) {

    public static UserAuthResponseDto of(
        String token, Long userId, Long channelId, String name, boolean onboardingCompleted) {
        return UserAuthResponseDto
            .builder()
            .token(token)
            .user(LineAuthUser.builder()
                .id(userId)
                .channelId(channelId)
                .name(name)
                .onboardingCompleted(onboardingCompleted)
                .build())
            .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private record LineAuthUser(
        Long id,
        Long channelId,
        String name,
        boolean onboardingCompleted
    ) {

    }

}
