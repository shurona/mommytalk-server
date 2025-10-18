package com.shrona.mommytalk.message.presentation.dtos.request;

import com.shrona.mommytalk.elevenlabs.infrastructure.sender.dto.ElevenLabsRequest;
import com.shrona.mommytalk.elevenlabs.infrastructure.sender.dto.ElevenLabsRequest.VoiceSettings;
import com.shrona.mommytalk.message.domain.type.AudioRole;
import java.util.Optional;

public record ContentAudioRequestDto(
    String text,
    String modelId,
    Long messageContentId,
    Double stability,
    Double similarityBoost,
    Double style,
    Double speed,
    Boolean speakerBoost,
    AudioRole audioRole
) {

    public ElevenLabsRequest toElevenLabsRequest() {
        return new ElevenLabsRequest(
            text,
            null,
            null,
            Optional.of(
                new VoiceSettings(
                    stability,
                    similarityBoost,
                    speakerBoost,
                    style,
                    speed
                )
            )
        );
    }
}
