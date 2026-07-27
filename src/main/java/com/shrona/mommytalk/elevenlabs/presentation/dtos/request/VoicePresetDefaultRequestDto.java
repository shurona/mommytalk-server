package com.shrona.mommytalk.elevenlabs.presentation.dtos.request;

/**
 * 기본 음성 지정/해제 요청 DTO
 * null = 해당 역할 미변경, true = 이 preset을 기본으로 지정(기존 기본 자동 해제), false = 이 preset의 기본 해제
 */
public record VoicePresetDefaultRequestDto(
    Boolean forMommy,
    Boolean forChild
) {

}
