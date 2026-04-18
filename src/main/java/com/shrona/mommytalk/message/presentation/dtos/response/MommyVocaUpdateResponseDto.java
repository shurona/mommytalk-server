package com.shrona.mommytalk.message.presentation.dtos.response;

import java.time.LocalDateTime;

public record MommyVocaUpdateResponseDto(
    Long messageTypeId,
    String mommyVoca,
    LocalDateTime updatedAt) {

}
