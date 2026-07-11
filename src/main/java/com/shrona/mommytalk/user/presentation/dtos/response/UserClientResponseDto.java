package com.shrona.mommytalk.user.presentation.dtos.response;

import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.type.OnBoardingStatus;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record UserClientResponseDto(
    Long userId,
    Long channelId,
    Integer userLevel,
    Integer childLevel,
    String name,
    String childName,
    Boolean onboardingCompleted,
    String phoneNumber,
    LocalTime preferredSendTime,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static UserClientResponseDto from(User user, Long channelId) {
        return UserClientResponseDto.builder()
            .userId(user.getId())
            .channelId(channelId)
            .userLevel(user.getUserLevel())
            .childLevel(user.getChildLevel())
            .name(user.getName())
            .childName(user.getChildName())
            .onboardingCompleted(OnBoardingStatus.isOnboarding(user.getOnboardingStatus()))
            .phoneNumber(user.getPhoneNumber().getPhoneNumber())
            .preferredSendTime(user.getPreferredSendTime())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

}
