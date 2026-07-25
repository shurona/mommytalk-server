package com.shrona.mommytalk.elevenlabs.presentation.dtos.request;

import com.shrona.mommytalk.elevenlabs.domain.type.Gender;

/**
 * 음성 프리셋 생성/수정 요청 DTO
 * sortOrder는 생성 시에만 사용(미지정 시 마지막에 추가), 수정 시 순서는 /order 엔드포인트로 처리
 */
public record VoicePresetRequestDto(
    String name,
    Gender gender,
    String voiceId,
    Integer sortOrder
) {

}
