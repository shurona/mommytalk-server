package com.shrona.mommytalk.message.presentation.controller;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelErrorCode;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.message.application.MessageContentService;
import com.shrona.mommytalk.message.application.MessageTypeService;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.presentation.dtos.request.BatchAudioRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.MessageTypeRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpdateAudioTextsRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.AudioTextsResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.BatchAudioResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageTypeInfoResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageTypeResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.UpdateAudioTextsResponseDto;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import com.shrona.mommytalk.message.presentation.dtos.request.UpdateMommyVocaRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MommyVocaUpdateResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/channels/{channelId}/messages/types")
@RestController
public class MessageTypeRestController {

    private final MessageTypeService messageTypeService;
    private final MessageContentService messageContentService;
    private final ChannelService channelService;

    @GetMapping
    public ApiResponse<MessageTypeResponseDto> getMessageType(
        @PathVariable Long channelId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {

        Optional<MessageTypeResponseDto> messageType = messageTypeService
            .findMessageTypeByChannelAndDate(channelId, date);

        return messageType
            .map(ApiResponse::success)
            .orElseGet(
                () -> ApiResponse.<MessageTypeResponseDto>error("MessageType not found", null));
    }

    /**
     * 메시지 Log의 날짜를 기준으로 조회
     */
    @GetMapping("/dates")
    public ApiResponse<MessageTypeInfoResponseDto> findMessageLogByDate(
        @PathVariable("channelId") Long channelId,
        @RequestParam("dateInfo")
        @DateTimeFormat(pattern = "yyyyMMdd") LocalDate dateInfo
    ) {

        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(ChannelErrorCode.CHANNEL_NOT_FOUND));

        MessageType messageTypeByDate = messageTypeService.findMessageTypeByDate(
            dateInfo, channel);

        if (messageTypeByDate == null) {
            return ApiResponse.success(null);
        }

        Map<String, Boolean> contentMap = messageContentService.groupMessageApprovedByLevel(
            messageTypeByDate);

        String mommyVoca = messageContentService.getMommyVocaForType(messageTypeByDate);

        return ApiResponse.success(
            MessageTypeInfoResponseDto.of(
                messageTypeByDate, mommyVoca, contentMap
            )
        );
    }

    @PostMapping
    public ApiResponse<MessageTypeResponseDto> createMessageType(
        @PathVariable Long channelId,
        @RequestBody MessageTypeRequestDto requestDto
    ) {

        MessageTypeResponseDto createdMessageType = messageTypeService
            .createMessageType(channelId, requestDto);

        return ApiResponse.success(createdMessageType);
    }

    @PutMapping
    public ApiResponse<MessageTypeResponseDto> updateMessageType(
        @PathVariable Long channelId,
        @RequestBody MessageTypeRequestDto requestDto
    ) {

        MessageTypeResponseDto updatedMessageType = messageTypeService
            .updateMessageType(channelId, requestDto);

        return ApiResponse.success(updatedMessageType);
    }

    @PatchMapping("/{messageTypeId}/mommy-voca")
    public ApiResponse<MommyVocaUpdateResponseDto> updateMommyVoca(
        @PathVariable Long channelId,
        @PathVariable Long messageTypeId,
        @RequestBody UpdateMommyVocaRequestDto requestDto
    ) {

        MommyVocaUpdateResponseDto result = messageContentService
            .updateMommyVocaForType(channelId, messageTypeId, requestDto);

        return ApiResponse.success(result);
    }

    @GetMapping("/{messageTypeId}/audio-texts")
    public ApiResponse<AudioTextsResponseDto> getAudioTexts(
        @PathVariable Long channelId,
        @PathVariable Long messageTypeId
    ) {
        return ApiResponse.success(
            messageContentService.getAudioTexts(channelId, messageTypeId));
    }

    @PutMapping("/{messageTypeId}/audio-texts")
    public ApiResponse<UpdateAudioTextsResponseDto> updateAudioTexts(
        @PathVariable Long channelId,
        @PathVariable Long messageTypeId,
        @RequestBody UpdateAudioTextsRequestDto requestDto
    ) {
        return ApiResponse.success(
            messageContentService.updateAudioTexts(channelId, messageTypeId, requestDto));
    }

    @PostMapping("/{messageTypeId}/audio/batch")
    public ApiResponse<BatchAudioResponseDto> batchCreateAudio(
        @PathVariable Long channelId,
        @PathVariable Long messageTypeId,
        @RequestBody BatchAudioRequestDto requestDto
    ) {
        return ApiResponse.success(
            messageContentService.batchCreateAudio(channelId, messageTypeId, requestDto));
    }

}
