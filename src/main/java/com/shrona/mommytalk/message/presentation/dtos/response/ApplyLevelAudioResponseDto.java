package com.shrona.mommytalk.message.presentation.dtos.response;

import java.util.List;

public record ApplyLevelAudioResponseDto(
    Long messageTypeId,
    String audioRole,
    Integer targetLevel,
    int successCount,
    int failureCount,
    List<ApplyLevelResultDto> results
) {

    public record ApplyLevelResultDto(
        Long contentId,
        Integer userLevel,
        Integer childLevel,
        boolean success,
        String fileUrl,
        String errorMessage
    ) {}
}
