package com.shrona.mommytalk.elevenlabs.infrastructure.sender.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

/**
 * ElevenLabs Text-to-Speech API 요청 DTO
 */
public record ElevenLabsRequest(
    String text,

    @JsonProperty("model_id")
    Optional<String> modelId,

    @JsonProperty("language_code")
    Optional<String> languageCode,

    @JsonProperty("voice_settings")
    Optional<VoiceSettings> voiceSettings
) {

    public static ElevenLabsRequest of(String text) {
        return new ElevenLabsRequest(
            text,
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
    }

    public static ElevenLabsRequest of(String text, String modelId) {
        return new ElevenLabsRequest(
            text,
            Optional.of(modelId),
            Optional.empty(),
            Optional.empty()
        );
    }

    /**
     * 음성 설정
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VoiceSettings(
        Double stability,
        @JsonProperty("similarity_boost")
        Double similarityBoost,
        @JsonProperty("speaker_boost")
        Boolean speakerBoost,
        Double style,
        Double speed
    ) {

        public static VoiceSettings defaults() {
            return new VoiceSettings(0.5, 0.75, true, null, null);
        }
    }
}
