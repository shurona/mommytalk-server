package com.shrona.mommytalk.message.presentation.dtos.response;

public record MessageContentAudioResponseDto(
    String fileUrl,
    String fileName
) {

    public static MessageContentAudioResponseDto of(String fileUrl, String fileName) {
        return new MessageContentAudioResponseDto(
            fileUrl, fileName
        );
    }

}
