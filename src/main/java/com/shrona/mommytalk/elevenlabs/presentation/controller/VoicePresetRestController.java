package com.shrona.mommytalk.elevenlabs.presentation.controller;

import static com.shrona.mommytalk.channel.common.exception.ChannelErrorCode.CHANNEL_NOT_FOUND;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.elevenlabs.application.VoicePresetService;
import com.shrona.mommytalk.elevenlabs.presentation.dtos.request.VoicePresetActiveRequestDto;
import com.shrona.mommytalk.elevenlabs.presentation.dtos.request.VoicePresetOrderRequestDto;
import com.shrona.mommytalk.elevenlabs.presentation.dtos.request.VoicePresetRequestDto;
import com.shrona.mommytalk.elevenlabs.presentation.dtos.response.VoicePresetResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 채널별 음성 프리셋 관리 API
 */
@RestController
@RequestMapping("/api/v1/channels/{channelId}/voices")
@RequiredArgsConstructor
public class VoicePresetRestController {

    private final VoicePresetService voicePresetService;
    private final ChannelService channelService;

    @GetMapping
    public ApiResponse<List<VoicePresetResponseDto>> getVoicePresets(
        @PathVariable Long channelId
    ) {
        Channel channel = findChannel(channelId);
        return ApiResponse.success(voicePresetService.findVoicePresets(channel));
    }

    @PostMapping
    public ApiResponse<VoicePresetResponseDto> createVoicePreset(
        @PathVariable Long channelId,
        @RequestBody VoicePresetRequestDto requestDto
    ) {
        Channel channel = findChannel(channelId);
        return ApiResponse.success(voicePresetService.createVoicePreset(channel, requestDto));
    }

    @PutMapping("/{voicePresetId}")
    public ApiResponse<VoicePresetResponseDto> updateVoicePreset(
        @PathVariable Long channelId,
        @PathVariable Long voicePresetId,
        @RequestBody VoicePresetRequestDto requestDto
    ) {
        Channel channel = findChannel(channelId);
        return ApiResponse.success(
            voicePresetService.updateVoicePreset(channel, voicePresetId, requestDto));
    }

    @DeleteMapping("/{voicePresetId}")
    public ApiResponse<Void> deleteVoicePreset(
        @PathVariable Long channelId,
        @PathVariable Long voicePresetId
    ) {
        Channel channel = findChannel(channelId);
        voicePresetService.deleteVoicePreset(channel, voicePresetId);
        return ApiResponse.success(null);
    }

    /**
     * 드래그 정렬용 순서 일괄 변경
     */
    @PutMapping("/order")
    public ApiResponse<List<VoicePresetResponseDto>> reorderVoicePresets(
        @PathVariable Long channelId,
        @RequestBody VoicePresetOrderRequestDto requestDto
    ) {
        Channel channel = findChannel(channelId);
        return ApiResponse.success(
            voicePresetService.reorderVoicePresets(channel, requestDto.voicePresetIds()));
    }

    /**
     * 활성/비활성 토글
     */
    @PatchMapping("/{voicePresetId}/active")
    public ApiResponse<VoicePresetResponseDto> updateActive(
        @PathVariable Long channelId,
        @PathVariable Long voicePresetId,
        @RequestBody VoicePresetActiveRequestDto requestDto
    ) {
        Channel channel = findChannel(channelId);
        return ApiResponse.success(
            voicePresetService.updateActive(channel, voicePresetId, requestDto.active()));
    }

    private Channel findChannel(Long channelId) {
        return channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));
    }

}
