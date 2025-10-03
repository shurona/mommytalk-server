package com.shrona.mommytalk.message.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.presentation.dtos.request.AiGenerateRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpdateTemplateRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.ContentStatusResponseDto;
import java.time.LocalDate;

public interface MessageContentService {

    /**
     * 메시지 컨텐츠 목록 조회
     */

    /**
     * AI 컨텐츠를 생성한다.
     */
    public MessageContent generateAiContent(Channel channel, AiGenerateRequestDto requestDto);

    /**
     * 메시지 컨텐츠를 업데이트한다.
     */
    public void updateMessageContent(Long channelId, Long contentId, UpdateTemplateRequestDto requestDto);

    /**
     * 메시지 컨텐츠를 승인한다.
     */
    public void approveMessageContent(Long channelId, Long contentId);

    /**
     * 특정 날짜의 콘텐츠 상태를 조회한다.
     */
    public ContentStatusResponseDto getContentStatus(Long channelId, LocalDate date);

}