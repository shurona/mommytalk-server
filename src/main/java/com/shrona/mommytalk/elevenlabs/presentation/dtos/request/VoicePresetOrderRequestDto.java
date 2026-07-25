package com.shrona.mommytalk.elevenlabs.presentation.dtos.request;

import java.util.List;

/**
 * 음성 프리셋 순서 일괄 변경 요청 DTO (드래그 정렬)
 * voicePresetIds: 원하는 순서대로 나열된 프리셋 id 목록 (해당 채널의 전체 프리셋과 일치해야 함)
 */
public record VoicePresetOrderRequestDto(
    List<Long> voicePresetIds
) {

}
