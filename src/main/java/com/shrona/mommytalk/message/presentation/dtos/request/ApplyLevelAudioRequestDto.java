package com.shrona.mommytalk.message.presentation.dtos.request;

import com.shrona.mommytalk.message.domain.type.AudioRole;

public record ApplyLevelAudioRequestDto(
    AudioRole audioRole,
    Integer targetLevel,
    SourceLevel source,
    String audioText,
    String modelId,
    Double speed,
    Long sourceContentId
) {

    public record SourceLevel(Integer userLevel, Integer childLevel) {}
}
