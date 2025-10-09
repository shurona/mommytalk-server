package com.shrona.mommytalk.openai.presentation.controller;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelErrorCode;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.openai.application.OpenAiService;
import com.shrona.mommytalk.openai.application.PromptService;
import com.shrona.mommytalk.openai.domain.MessagePrompt;
import com.shrona.mommytalk.openai.domain.type.PromptType;
import com.shrona.mommytalk.openai.infrastructure.repository.dto.PromptHistoryDto;
import com.shrona.mommytalk.openai.infrastructure.repository.query.MessagePromptQueryRepository;
import com.shrona.mommytalk.openai.presentation.dtos.request.InsertPromptRequestBody;
import com.shrona.mommytalk.openai.presentation.dtos.request.UpdatePromptRequestBody;
import com.shrona.mommytalk.openai.presentation.dtos.response.PromptHistoryCollectResponseDto;
import com.shrona.mommytalk.openai.presentation.dtos.response.PromptHistoryResponseDto;
import com.shrona.mommytalk.openai.presentation.dtos.response.PromptResponseDto;
import com.shrona.mommytalk.openai.presentation.dtos.response.SelectedPromptResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/channels/{channelId}")
public class MessagePromptRestController {

    private final OpenAiService openAiService;
    private final PromptService promptService;
    private final ChannelService channelService;

    private final MessagePromptQueryRepository messagePromptQueryRepository;


    @GetMapping("/test")
    public String testOpenAi() {
        return openAiService.testPrompt();
    }


    /**
     * 프롬프트 목록 조회
     */
    @GetMapping("/prompt")
    public ApiResponse<List<SelectedPromptResponseDto>> findPrompt(
        @PathVariable("channelId") Long channelId
    ) {

        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(ChannelErrorCode.CHANNEL_NOT_FOUND));

        List<MessagePrompt> selectedPromptList = messagePromptQueryRepository.findSelectedPromptList(
            channel);

        return ApiResponse.success(
            selectedPromptList.stream().map(SelectedPromptResponseDto::of).toList()
        );

    }

    /**
     * 프롬프트 단일 조회
     */
    @GetMapping("/prompt/{promptId}")
    public ApiResponse<PromptResponseDto> findPromptById(
        @PathVariable("channelId") Long channelId,
        @PathVariable("promptId") Long promptId
    ) {

        MessagePrompt prompt = promptService.findById(promptId);

        return ApiResponse.success(
            PromptResponseDto.of(prompt)
        );

    }

    /**
     * 프롬프트 History 목록 조회
     */
    @GetMapping("/prompt/history")
    public ApiResponse<PromptHistoryCollectResponseDto> findPromptHistory(
        @PathVariable("channelId") Long channelId
    ) {

        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(ChannelErrorCode.CHANNEL_NOT_FOUND));

        List<PromptHistoryDto> basicHistoryList = messagePromptQueryRepository.findPromptHistoryList(
            channel, PromptType.BASIC);
        List<PromptHistoryDto> advancedHistoryList = messagePromptQueryRepository.findPromptHistoryList(
            channel, PromptType.ADVANCE);

        return ApiResponse.success(PromptHistoryCollectResponseDto.of(
            basicHistoryList.stream().map(PromptHistoryResponseDto::of).toList(),
            advancedHistoryList.stream().map(PromptHistoryResponseDto::of).toList()
        ));

    }

    /**
     * 프롬프트를 등록한다.
     */
    @PostMapping("/prompt")
    public ApiResponse<Long> insertPromptInfo(
        @PathVariable("channelId") Long channelId,
        @RequestBody InsertPromptRequestBody requestBody
    ) {

        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(ChannelErrorCode.CHANNEL_NOT_FOUND));

        Long pId = promptService.insertPromptInfo(
            channel, requestBody.label(), requestBody.prompt(), requestBody.type());

        return ApiResponse.success(pId);
    }

    /**
     * 프롬프트를 수정한다.
     */
    @PutMapping("/prompt/{promptId}")
    public ApiResponse<Long> updatePromptInfo(
        @PathVariable("channelId") Long channelId,
        @PathVariable("promptId") Long promptId,
        @RequestBody UpdatePromptRequestBody requestBody
    ) {

        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(ChannelErrorCode.CHANNEL_NOT_FOUND));

        Long pId = promptService.updatePromptInfo(channel, promptId, requestBody.label(),
            requestBody.prompt());

        return ApiResponse.success(pId);
    }

    /**
     * 프롬프트를 등록한다.
     */
    @PatchMapping("/prompt/{promptId}/register")
    public ApiResponse<Long> updatePromptInfo(
        @PathVariable("channelId") Long channelId,
        @PathVariable("promptId") Long promptId
    ) {

        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(ChannelErrorCode.CHANNEL_NOT_FOUND));

        Long pId = promptService.registerPromptInfo(channel, promptId);

        return ApiResponse.success(pId);
    }
}