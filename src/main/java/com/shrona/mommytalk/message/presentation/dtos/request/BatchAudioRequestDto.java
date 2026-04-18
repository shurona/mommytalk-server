package com.shrona.mommytalk.message.presentation.dtos.request;

public record BatchAudioRequestDto(
    AudioModelConfig mommy,
    AudioModelConfig child
) {

    public record AudioModelConfig(String modelId, Double speed) {

    }
}
