package com.shrona.mommytalk.elevenlabs.application;

import static com.shrona.mommytalk.elevenlabs.common.exception.ElevenLabsErrorCode.VOICE_PRESET_NOT_FOUND;
import static com.shrona.mommytalk.elevenlabs.common.exception.ElevenLabsErrorCode.VOICE_PRESET_ORDER_MISMATCH;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.elevenlabs.common.exception.ElevenLabsException;
import com.shrona.mommytalk.elevenlabs.domain.VoicePreset;
import com.shrona.mommytalk.elevenlabs.infrastructure.reposiotry.VoicePresetRepository;
import com.shrona.mommytalk.elevenlabs.presentation.dtos.request.VoicePresetRequestDto;
import com.shrona.mommytalk.elevenlabs.presentation.dtos.response.VoicePresetResponseDto;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class VoicePresetServiceImpl implements VoicePresetService {

    private final VoicePresetRepository voicePresetRepository;

    @Override
    public List<VoicePresetResponseDto> findVoicePresets(Channel channel) {
        return voicePresetRepository.findByChannelOrderBySortOrderAsc(channel).stream()
            .map(VoicePresetResponseDto::of)
            .toList();
    }

    @Override
    @Transactional
    public VoicePresetResponseDto createVoicePreset(
        Channel channel, VoicePresetRequestDto requestDto) {

        int sortOrder = requestDto.sortOrder() != null
            ? requestDto.sortOrder()
            : nextSortOrder(channel);

        VoicePreset preset = VoicePreset.of(
            channel, requestDto.name(), requestDto.gender(), requestDto.voiceId(), sortOrder);

        return VoicePresetResponseDto.of(voicePresetRepository.save(preset));
    }

    @Override
    @Transactional
    public VoicePresetResponseDto updateVoicePreset(
        Channel channel, Long voicePresetId, VoicePresetRequestDto requestDto) {

        VoicePreset preset = findByIdAndChannel(channel, voicePresetId);
        preset.update(requestDto.name(), requestDto.gender(), requestDto.voiceId());

        return VoicePresetResponseDto.of(preset);
    }

    @Override
    @Transactional
    public void deleteVoicePreset(Channel channel, Long voicePresetId) {
        VoicePreset preset = findByIdAndChannel(channel, voicePresetId);
        preset.markAsDeleted();
    }

    @Override
    @Transactional
    public List<VoicePresetResponseDto> reorderVoicePresets(
        Channel channel, List<Long> orderedIds) {

        List<VoicePreset> presets = voicePresetRepository.findByChannelOrderBySortOrderAsc(channel);
        Map<Long, VoicePreset> byId = presets.stream()
            .collect(Collectors.toMap(VoicePreset::getId, Function.identity()));

        // 요청 id 집합이 채널의 전체 프리셋과 정확히 일치하는지 검증
        if (orderedIds.size() != presets.size() || !byId.keySet().containsAll(orderedIds)) {
            throw new ElevenLabsException(VOICE_PRESET_ORDER_MISMATCH);
        }

        for (int i = 0; i < orderedIds.size(); i++) {
            byId.get(orderedIds.get(i)).updateSortOrder(i);
        }

        return findVoicePresets(channel);
    }

    @Override
    @Transactional
    public VoicePresetResponseDto updateActive(
        Channel channel, Long voicePresetId, boolean active) {

        VoicePreset preset = findByIdAndChannel(channel, voicePresetId);
        preset.updateActive(active);

        return VoicePresetResponseDto.of(preset);
    }

    private VoicePreset findByIdAndChannel(Channel channel, Long voicePresetId) {
        return voicePresetRepository.findByIdAndChannel(voicePresetId, channel)
            .orElseThrow(() -> new ElevenLabsException(VOICE_PRESET_NOT_FOUND));
    }

    private int nextSortOrder(Channel channel) {
        return voicePresetRepository.findByChannelOrderBySortOrderAsc(channel).stream()
            .mapToInt(VoicePreset::getSortOrder)
            .max()
            .orElse(-1) + 1;
    }

}
