package com.shrona.mommytalk.elevenlabs.presentation.dtos.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shrona.mommytalk.elevenlabs.infrastructure.sender.dto.ElevenLabsRequest;
import com.shrona.mommytalk.elevenlabs.infrastructure.sender.dto.ElevenLabsRequest.VoiceSettings;
import java.util.Optional;

/**
 * 오디오 생성 요청 DTO
 */
public record GenerateAudioRequestDto(
    String text,
    Long messageContentId,
    Optional<String> modelId,
    Optional<String> languageCode,
    Optional<VoiceSettingsDto> voiceSettings
) {

    public ElevenLabsRequest toElevenLabsRequest() {
        return new ElevenLabsRequest(
            text,
            modelId,
            languageCode,
            voiceSettings.map(VoiceSettingsDto::toVoiceSettings)
        );
    }

    /**
     * 음성 설정 DTO
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VoiceSettingsDto(
        Double stability,
        Double similarityBoost,
        Boolean speakerBoost,
        Double style,
        Double speed
    ) {

        public VoiceSettings toVoiceSettings() {
            return new VoiceSettings(
                stability,
                similarityBoost,
                speakerBoost,
                style,
                speed
            );
        }
    }
}
