package com.shrona.mommytalk.message.presentation.dtos.response;

import java.time.LocalDateTime;

public record UpdateAudioTextsResponseDto(
    Long messageTypeId,
    int updatedCount,
    LocalDateTime updatedAt
) {

}
