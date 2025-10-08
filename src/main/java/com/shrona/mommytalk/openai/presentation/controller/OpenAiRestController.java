package com.shrona.mommytalk.openai.presentation.controller;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelErrorCode;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.openai.application.OpenAiService;
import com.shrona.mommytalk.openai.application.PromptService;
import com.shrona.mommytalk.openai.domain.MessagePrompt;
import com.shrona.mommytalk.openai.presentation.dtos.response.MessagePromptResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/channels/{channelId}")
public class OpenAiRestController {

    private final OpenAiService openAiService;
    private final PromptService promptService;
    private final ChannelService channelService;


    @GetMapping("/test")
    public String testOpenAi() {
        return openAiService.testPrompt();
    }


    @GetMapping("/prompt")
    public ApiResponse<MessagePromptResponseDto> findPrompt(
        @PathVariable("channelId") Long channelId
    ) {

        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(ChannelErrorCode.CHANNEL_NOT_FOUND));

        MessagePrompt messagePrompt = promptService.findMessagePromptList(channel);

        return ApiResponse.success(MessagePromptResponseDto.of(messagePrompt));

    }

}