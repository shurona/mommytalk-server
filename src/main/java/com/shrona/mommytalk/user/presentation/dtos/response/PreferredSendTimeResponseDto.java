package com.shrona.mommytalk.user.presentation.dtos.response;

import com.shrona.mommytalk.user.domain.User;
import java.time.LocalTime;

public record PreferredSendTimeResponseDto(
    LocalTime preferredSendTime,
    LocalTime pendingPreferredSendTime
) {

    public static PreferredSendTimeResponseDto from(User user) {
        return new PreferredSendTimeResponseDto(
            user.getPreferredSendTime(),
            user.getPendingPreferredSendTime()
        );
    }
}
