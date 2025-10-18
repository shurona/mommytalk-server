package com.shrona.mommytalk.elevenlabs.presentation.dtos.response;

/**
 * 오디오 생성 응답 DTO
 */
public record GenerateAudioResponseDto(
    Long messageContentId,
    String filePath,
    String message
) {

    public static GenerateAudioResponseDto of(Long messageContentId, String filePath) {
        return new GenerateAudioResponseDto(
            messageContentId,
            filePath,
            "오디오 생성 완료"
        );
    }
}
