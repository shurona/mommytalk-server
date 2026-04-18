package com.shrona.mommytalk.message.presentation.dtos.response;

import java.time.LocalDateTime;

public record ContentMommyVocaUpdateResponseDto(
    Long contentId,
    String mommyVoca,
    LocalDateTime updatedAt) {

}
