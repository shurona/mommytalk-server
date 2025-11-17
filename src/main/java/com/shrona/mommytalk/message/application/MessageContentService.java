package com.shrona.mommytalk.message.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.elevenlabs.domain.ElevenLabsMedia;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.presentation.dtos.request.AiGenerateRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.ContentAudioRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpsertMessageContentRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.ContentStatusResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageContentResponseDto;
import java.time.LocalDate;
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
     * 사용자가 받은 메시지 컨텐츠 조회 (MOMMYVOCA 권한 확인 포함)
     */
    MessageContentResponseDto findContentForUser(
        Long channelId, Long userId, Long messageLogDetailId);
}