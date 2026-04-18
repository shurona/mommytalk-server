package com.shrona.mommytalk.message.presentation.dtos.response;

import java.util.List;

public record BatchAudioResponseDto(
    int successCount,
    int failureCount,
    List<BatchAudioResultDto> results
) {

    public record BatchAudioResultDto(
        Long contentId,
        Integer userLevel,
        Integer childLevel,
        String audioRole,
        boolean success,
        String fileUrl,
        String errorMessage
    ) {

    }
}
