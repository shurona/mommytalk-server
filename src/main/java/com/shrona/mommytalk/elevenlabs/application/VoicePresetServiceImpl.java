package com.shrona.mommytalk.elevenlabs.application;

import static com.shrona.mommytalk.elevenlabs.common.exception.ElevenLabsErrorCode.VOICE_PRESET_DEFAULT_MUST_BE_ACTIVE;
import static com.shrona.mommytalk.elevenlabs.common.exception.ElevenLabsErrorCode.VOICE_PRESET_DUPLICATE_VOICE_ID;
import static com.shrona.mommytalk.elevenlabs.common.exception.ElevenLabsErrorCode.VOICE_PRESET_IS_DEFAULT;
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
import java.util.Optional;
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

        // 같은 채널 내 voiceId 중복 방지 (soft delete 된 건 @SQLRestriction 으로 제외됨)
        voicePresetRepository.findByChannelAndVoiceId(channel, requestDto.voiceId())
            .ifPresent(existing -> {
                throw new ElevenLabsException(VOICE_PRESET_DUPLICATE_VOICE_ID);
            });

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

        // 같은 채널 내 voiceId 중복 방지 (자기 자신은 예외)
        voicePresetRepository.findByChannelAndVoiceId(channel, requestDto.voiceId())
            .filter(existing -> !existing.getId().equals(voicePresetId))
            .ifPresent(existing -> {
                throw new ElevenLabsException(VOICE_PRESET_DUPLICATE_VOICE_ID);
            });

        preset.update(requestDto.name(), requestDto.gender(), requestDto.voiceId());

        return VoicePresetResponseDto.of(preset);
    }

    @Override
    @Transactional
    public void deleteVoicePreset(Channel channel, Long voicePresetId) {
        VoicePreset preset = findByIdAndChannel(channel, voicePresetId);

        // 기본 음성은 삭제 불가 (먼저 다른 음성을 기본으로 지정해야 함)
        if (preset.isDefault()) {
            throw new ElevenLabsException(VOICE_PRESET_IS_DEFAULT);
        }

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

        // 기본 음성은 비활성화 불가 (먼저 다른 음성을 기본으로 지정해야 함)
        if (!active && preset.isDefault()) {
            throw new ElevenLabsException(VOICE_PRESET_IS_DEFAULT);
        }

        preset.updateActive(active);

        return VoicePresetResponseDto.of(preset);
    }

    @Override
    @Transactional
    public VoicePresetResponseDto updateDefault(
        Channel channel, Long voicePresetId, Boolean forMommy, Boolean forChild) {

        VoicePreset preset = findByIdAndChannel(channel, voicePresetId);

        if (forMommy != null) {
            applyDefault(channel, preset, forMommy, true);
        }
        if (forChild != null) {
            applyDefault(channel, preset, forChild, false);
        }

        return VoicePresetResponseDto.of(preset);
    }

    /**
     * 특정 역할(엄마/아이)의 기본 지정/해제. 지정 시 활성 필수, 기존 기본은 자동 해제.
     */
    private void applyDefault(Channel channel, VoicePreset preset, boolean value, boolean mommy) {
        if (!value) {
            markDefault(preset, false, mommy);
            return;
        }

        // 지정 대상은 활성 상태여야 함 (삭제된 건 findByIdAndChannel 에서 이미 제외)
        if (!Boolean.TRUE.equals(preset.getIsActive())) {
            throw new ElevenLabsException(VOICE_PRESET_DEFAULT_MUST_BE_ACTIVE);
        }

        // 기존 기본을 먼저 해제하고 flush → 부분 unique 인덱스 충돌 방지
        findCurrentDefault(channel, mommy)
            .filter(current -> !current.getId().equals(preset.getId()))
            .ifPresent(current -> markDefault(current, false, mommy));
        voicePresetRepository.flush();

        markDefault(preset, true, mommy);
    }

    private Optional<VoicePreset> findCurrentDefault(Channel channel, boolean mommy) {
        return mommy
            ? voicePresetRepository.findByChannelAndDefaultForMommyTrue(channel)
            : voicePresetRepository.findByChannelAndDefaultForChildTrue(channel);
    }

    private void markDefault(VoicePreset preset, boolean value, boolean mommy) {
        if (mommy) {
            preset.markDefaultForMommy(value);
        } else {
            preset.markDefaultForChild(value);
        }
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
