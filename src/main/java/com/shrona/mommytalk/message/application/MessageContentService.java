package com.shrona.mommytalk.message.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.presentation.dtos.request.AiGenerateRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpdateTemplateRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.ContentStatusResponseDto;
import java.time.LocalDate;
import java.util.Map;

public interface MessageContentService {

    /**
     * AI 컨텐츠를 생성한다.
     */
    MessageContent generateAiContent(Channel channel, AiGenerateRequestDto requestDto);

    /**
     * 메시지 컨텐츠를 업데이트한다.
     */
    void updateMessageContent(Long channelId, Long contentId,
        UpdateTemplateRequestDto requestDto);

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

}