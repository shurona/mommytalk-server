package com.shrona.mommytalk.auth.presentation.dtos.response;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record LineAuthResponseDto(
    String token,
    LineAuthUser user
) {

    public static LineAuthResponseDto of(
        String token, Long userId, String name, boolean onboardingCompleted) {
        return LineAuthResponseDto
            .builder()
            .token(token)
            .user(LineAuthUser.builder()
                .id(userId)
                .name(name)
                .onboardingCompleted(onboardingCompleted)
                .build())
            .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private record LineAuthUser(
        Long id,
        String name,
        boolean onboardingCompleted
    ) {

    }

}
