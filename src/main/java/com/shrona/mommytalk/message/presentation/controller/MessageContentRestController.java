package com.shrona.mommytalk.message.presentation.controller;


import static com.shrona.mommytalk.channel.common.exception.ChannelErrorCode.CHANNEL_NOT_FOUND;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.application.MessageContentService;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.presentation.dtos.request.AiGenerateRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpdateTemplateRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.AiGenerateResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.ContentStatusResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageContentResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.UpdateContentResponseDto;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/channels/{channelId}/contents")
public class MessageContentRestController {

    private final MessageContentService messageContentService;
    private final ChannelService channelService;

    @PostMapping("/generate")
    public AiGenerateResponseDto generateAiContent(
        @PathVariable Long channelId,
        @RequestBody AiGenerateRequestDto requestDto) {

        // 채널 정보 갖고 온다.
        Channel channelInfo = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        // AI 컨텐츠 생성
        MessageContent generatedContent = messageContentService.generateAiContent(channelInfo,
            requestDto);

        // Response DTO 생성
        MessageContentResponseDto contentDto = MessageContentResponseDto.of(generatedContent,
            requestDto.language());

        return AiGenerateResponseDto.of(contentDto);
    }


    @PostMapping("/{contentId}/test")
    public void testDelivery() {

    }

    @PatchMapping("/{contentId}")
    public UpdateContentResponseDto updateAiContentTemplate(
        @PathVariable Long channelId,
        @PathVariable("contentId") Long contentId,
        @RequestBody UpdateTemplateRequestDto requestDto
    ) {

        messageContentService.updateMessageContent(channelId, contentId, requestDto);

        return UpdateContentResponseDto.success();
    }

    @PatchMapping("/{contentId}/approve")
    public UpdateContentResponseDto approveContent(
        @PathVariable Long channelId,
        @PathVariable("contentId") Long contentId
    ) {

        messageContentService.approveMessageContent(channelId, contentId);

        return UpdateContentResponseDto.approved();
    }

    @GetMapping("/status")
    public ContentStatusResponseDto getContentStatus(
        @PathVariable Long channelId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {

        return messageContentService.getContentStatus(channelId, date);
    }

}
