package com.shrona.mommytalk.user.presentation.dtos.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record UpdatePreferredSendTimeRequestDto(
    @NotNull
    LocalTime preferredSendTime
) {

}
