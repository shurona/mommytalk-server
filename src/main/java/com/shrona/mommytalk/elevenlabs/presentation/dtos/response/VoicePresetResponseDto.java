package com.shrona.mommytalk.elevenlabs.presentation.dtos.response;

import com.shrona.mommytalk.elevenlabs.domain.VoicePreset;
import com.shrona.mommytalk.elevenlabs.domain.type.Gender;

/**
 * 음성 프리셋 응답 DTO
 */
public record VoicePresetResponseDto(
    Long id,
    String name,
    Gender gender,
    String voiceId,
    int sortOrder,
    boolean active
) {

    public static VoicePresetResponseDto of(VoicePreset preset) {
        return new VoicePresetResponseDto(
            preset.getId(),
            preset.getName(),
            preset.getGender(),
            preset.getVoiceId(),
            preset.getSortOrder(),
            preset.getIsActive()
        );
    }

}
