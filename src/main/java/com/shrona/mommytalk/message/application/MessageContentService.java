package com.shrona.mommytalk.message.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.elevenlabs.domain.ElevenLabsMedia;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.presentation.dtos.request.AiGenerateRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.ApplyLevelAudioRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.BatchAudioRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.BulkImportMessageRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.ContentAudioRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpdateAudioTextsRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpdateMommyVocaRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpsertMessageContentRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.AudioTextsResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.ApplyLevelAudioResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.BatchAudioResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.ContentMommyVocaUpdateResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.ContentStatusResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageContentResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MommyVocaUpdateResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.UpdateAudioTextsResponseDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MessageContentService {

    /**
     * MessageContent 단일 조회
     */
    MessageContent findById(Long id);

    /**
     * MessageContent type과 유저 레벨을 기준으로 조회
     */
    MessageContent findByTypeAndUserLevel(Long typeId, Integer userLevel, Integer childLevel);

    /**
     * AI 컨텐츠를 생성한다.
     */
    MessageContent generateAiContent(Channel channel, AiGenerateRequestDto requestDto);

    /**
     * 메시지 컨텐츠를 생성 또는 업데이트한다.
     */
    Long upsertMessageContent(Long channelId, UpsertMessageContentRequestDto requestDto);

    /**
     * Content의 오디오를 업데이트 해준다.
     */
    ElevenLabsMedia updateContentAudio(Channel channelInfo, Long contentId,
        ContentAudioRequestDto requestDto);

    /**
     * 메시지 컨텐츠를 승인한다.
     */
    void approveMessageContent(Long channelId, Long contentId);

    /**
     * 특정 날짜의 콘텐츠 상태를 조회한다.
     */
    ContentStatusResponseDto getContentStatus(Long channelId, LocalDate date);

    /**
     * MessageType의 MessageContent 목록을
     * userLevel_childLevel : MessageContent 형식으로 변환해준다.
     */
    Map<String, MessageContent> groupMessageContentByLevel(MessageType messageType);

    /**
     * MessageType의 MessageContent 목록을
     * userLevel_childLevel : content(String) 형식으로 변환해준다.
     */
    Map<String, String> groupMessageTextByLevel(MessageType messageType);

    /**
     * MessageType의 MessageContent 목록을
     * userLevel_childLevel : approved(Boolean) 형식으로 변환해준다.
     */
    Map<String, Boolean> groupMessageApprovedByLevel(MessageType messageType);

    /**
     * MessageType의 9개 content 중 공통 mommyVoca 값을 반환한다.
     */
    String getMommyVocaForType(MessageType messageType);

    /**
     * MessageType에 속한 전체 content의 mommyVoca를 일괄 업데이트한다.
     */
    MommyVocaUpdateResponseDto updateMommyVocaForType(Long channelId, Long messageTypeId,
        UpdateMommyVocaRequestDto requestDto);

    /**
     * 특정 MessageContent의 mommyVoca만 단건 업데이트한다.
     */
    ContentMommyVocaUpdateResponseDto updateMommyVocaForContent(Long channelId, Long contentId,
        UpdateMommyVocaRequestDto requestDto);

    /**
     * 사용자가 받은 메시지 컨텐츠 조회 (MOMMYVOCA 권한 확인 포함)
     */
    MessageContentResponseDto findContentForUser(
        Long channelId, Long userId, Long messageLogDetailId);

    /**
     * 레거시 데이터 벌크 임포트 (userLevel=2, childLevel=2 고정)
     */
    void bulkImportLegacyData(Channel channel, List<BulkImportMessageRequestDto> requests);

    /**
     * 레거시 MP3 파일을 R2에 업로드하고 MessageContent에 연결
     * (CSV 기반, userLevel=2/childLevel=2 고정)
     */
    int uploadLegacyAudio(Channel channel, int year, int month);

    /**
     * 9개 레벨 매트릭스의 오디오 텍스트/URL 정보를 조회한다.
     */
    AudioTextsResponseDto getAudioTexts(Long channelId, Long messageTypeId);

    /**
     * 9개 레벨 매트릭스의 오디오 텍스트를 저장한다 (부분 items 허용).
     */
    UpdateAudioTextsResponseDto updateAudioTexts(Long channelId, Long messageTypeId,
        UpdateAudioTextsRequestDto req);

    /**
     * 저장된 텍스트 기준으로 오디오를 일괄 생성한다.
     */
    BatchAudioResponseDto batchCreateAudio(Long channelId, Long messageTypeId,
        BatchAudioRequestDto req);

    /**
     * 샘플 음성을 같은 레벨 전체에 적용한다 (URL 공유 방식).
     */
    ApplyLevelAudioResponseDto applyLevelAudio(Long channelId, Long messageTypeId,
        ApplyLevelAudioRequestDto requestDto);
}