package com.shrona.mommytalk.user.presentation.dtos.request;

public record GenerateSentenceRequestDto(
    Long channelId,
    String sentence
) {

}
