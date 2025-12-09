package com.shrona.mommytalk.message.presentation.controller;

import com.shrona.mommytalk.common.annotation.CurrentUserId;
import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.message.application.MessageContentService;
import com.shrona.mommytalk.message.application.MessageService;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageContentResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageHistoryResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/client/channels/{channelId}/messages")
@RestController
public class MessageHistoryClientRestController {

    private final MessageService messageService;
    private final MessageContentService messageContentService;

    /**
     * 사용자가 받은 메시지 히스토리 조회 (연월 기준, 날짜 내림차순)
     *
     * @param channelId 채널 ID
     * @param userId    사용자 ID (JWT 토큰에서 자동 추출)
     * @param year      조회 연도
     * @param month     조회 월 (1-12)
     * @return 메시지 히스토리 목록
     */
    @GetMapping("/history")
    public ApiResponse<List<MessageHistoryResponseDto>> getMessageHistory(
        @PathVariable Long channelId,
        @CurrentUserId Long userId,
        @RequestParam int year,
        @RequestParam int month
    ) {
        List<MessageHistoryResponseDto> history = messageService.findMessageHistory(
            channelId, userId, year, month);

        return ApiResponse.success(history);
    }

    /**
     * 사용자가 받은 메시지 컨텐츠 단일 조회 (MOMMYVOCA 권한 확인 포함)
     *
     * @param channelId          채널 ID
     * @param userId             사용자 ID (JWT 토큰에서 자동 추출)
     * @param messageLogDetailId 메시지 logdetail ID
     * @return 메시지 컨텐츠 상세
     */
    @GetMapping("/logs/{messageLogDetailId}")
    public ApiResponse<MessageContentResponseDto> getMessageContent(
        @PathVariable Long channelId,
        @CurrentUserId Long userId,
        @PathVariable Long messageLogDetailId
    ) {

        MessageContentResponseDto content = messageContentService.findContentForUser(
            channelId, userId, messageLogDetailId);

        return ApiResponse.success(content);
    }
}
