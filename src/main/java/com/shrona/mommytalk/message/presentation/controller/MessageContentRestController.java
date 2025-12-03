package com.shrona.mommytalk.message.presentation.controller;


import static com.shrona.mommytalk.channel.common.exception.ChannelErrorCode.CHANNEL_NOT_FOUND;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.elevenlabs.domain.ElevenLabsMedia;
import com.shrona.mommytalk.kakao.application.sender.KakaoMessageSender;
import com.shrona.mommytalk.line.application.sender.LineMessageSender;
import com.shrona.mommytalk.message.application.MessageContentService;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.presentation.dtos.request.AiGenerateRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.BulkImportMessageRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.ContentAudioRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.request.UpsertMessageContentRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.ContentStatusResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageContentAudioResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageContentResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.UpdateContentResponseDto;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/channels/{channelId}/contents")
public class MessageContentRestController {

    private final MessageContentService messageContentService;
    private final ChannelService channelService;

    private final LineMessageSender lineMessageSender;
    private final KakaoMessageSender kakaoMessageSender;


    @GetMapping
    public ApiResponse<MessageContentResponseDto> findByTypeAndLevel(
        @PathVariable Long channelId,
        @RequestParam("type") Long typeId,
        @RequestParam("child") int childLevel,
        @RequestParam("user") int userLevel
    ) {
        // 채널 정보 갖고 온다.
        Channel channelInfo = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        MessageContent content =
            messageContentService.findByTypeAndUserLevel(typeId, userLevel, childLevel);

        if (content == null) {
            return ApiResponse.success(null);
        }

        return ApiResponse.success(MessageContentResponseDto.of(content));
    }

    @GetMapping("/{contentId}")
    public ApiResponse<MessageContentResponseDto> findById(
        @PathVariable Long channelId,
        @PathVariable Long contentId
    ) {
        // 채널 정보 갖고 온다.
        Channel channelInfo = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        MessageContent content = messageContentService.findById(contentId);

        return ApiResponse.success(MessageContentResponseDto.of(content));
    }

    @PostMapping("/generate")
    public ApiResponse<Long> generateAiContent(
        @PathVariable Long channelId,
        @RequestBody AiGenerateRequestDto requestDto
    ) {

        // 채널 정보 갖고 온다.
        Channel channelInfo = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        // AI 컨텐츠 생성
        MessageContent generatedContent = messageContentService.generateAiContent(channelInfo,
            requestDto);

        // Response DTO 생성
        MessageContentResponseDto contentDto = MessageContentResponseDto.of(generatedContent);

        return ApiResponse.success(contentDto.id());
    }


    @PostMapping("/{contentId}/test")
    public ApiResponse<Boolean> testDelivery(
        @PathVariable Long channelId,
        @PathVariable Long contentId
    ) {
        Channel channelInfo = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        switch (channelInfo.getChannelPlatform()) {
            case LINE -> lineMessageSender.sendTestLineMessage(channelInfo, contentId);
            case KAKAO -> kakaoMessageSender.sendTestMessage(channelInfo, contentId);
        }

        return ApiResponse.success(true);
    }

    @PostMapping
    public ApiResponse<Long> upsertMessageContent(
        @PathVariable Long channelId,
        @RequestBody UpsertMessageContentRequestDto requestDto
    ) {
        Long id = messageContentService.upsertMessageContent(channelId, requestDto);

        return ApiResponse.success(id);
    }

    @PostMapping("/{contentId}/audio")
    public ApiResponse<MessageContentAudioResponseDto> insertAudio(
        @PathVariable Long channelId,
        @PathVariable Long contentId,
        @RequestBody ContentAudioRequestDto requestDto
    ) {
        Channel channelInfo = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        log.info("오디오 생성 요청 - messageContentId: {}, text length: {}",
            requestDto.messageContentId(), requestDto.text().length());

        ElevenLabsMedia elevenLabsMedia = messageContentService.updateContentAudio(channelInfo,
            contentId, requestDto);

        return ApiResponse.success(MessageContentAudioResponseDto.of(
            elevenLabsMedia.getFileUrl(), elevenLabsMedia.getFileName()));
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

    /**
     * 레거시 데이터 벌크 임포트 (userLevel=2, childLevel=2 고정)
     */
    @PostMapping("/bulk-import")
    public ApiResponse<String> bulkImportLegacyData(
        @PathVariable Long channelId,
        @Valid @RequestBody List<BulkImportMessageRequestDto> requests
    ) {
        log.info("[레거시 데이터 임포트 API 호출] channelId={}, 데이터 개수={}",
            channelId, requests.size());

        Channel channelInfo = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        messageContentService.bulkImportLegacyData(channelInfo, requests);

        return ApiResponse.success("총 " + requests.size() + "건의 데이터가 임포트되었습니다.");
    }
}
