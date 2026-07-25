package com.shrona.mommytalk.elevenlabs.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.elevenlabs.presentation.dtos.request.VoicePresetRequestDto;
import com.shrona.mommytalk.elevenlabs.presentation.dtos.response.VoicePresetResponseDto;
import java.util.List;

/**
 * 채널별 음성 프리셋 관리 서비스
 */
public interface VoicePresetService {

    /**
     * 채널의 음성 프리셋 목록 조회 (sortOrder 오름차순)
     */
    List<VoicePresetResponseDto> findVoicePresets(Channel channel);

    /**
     * 음성 프리셋 생성
     */
    VoicePresetResponseDto createVoicePreset(Channel channel, VoicePresetRequestDto requestDto);

    /**
     * 음성 프리셋 수정
     */
    VoicePresetResponseDto updateVoicePreset(
        Channel channel, Long voicePresetId, VoicePresetRequestDto requestDto);

    /**
     * 음성 프리셋 삭제 (논리 삭제)
     */
    void deleteVoicePreset(Channel channel, Long voicePresetId);

    /**
     * 음성 프리셋 순서 일괄 변경
     */
    List<VoicePresetResponseDto> reorderVoicePresets(Channel channel, List<Long> orderedIds);

    /**
     * 음성 프리셋 활성/비활성 변경
     */
    VoicePresetResponseDto updateActive(Channel channel, Long voicePresetId, boolean active);

}
