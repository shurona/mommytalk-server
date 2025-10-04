package com.shrona.mommytalk.message.presentation.controller;

import static com.shrona.mommytalk.line.presentation.form.TargetType.ALL;
import static com.shrona.mommytalk.line.presentation.form.TargetType.GROUP;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelErrorCode;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.common.dto.PageResponseDto;
import com.shrona.mommytalk.message.application.MessageContentService;
import com.shrona.mommytalk.message.application.MessageLogDetailService;
import com.shrona.mommytalk.message.application.MessageService;
import com.shrona.mommytalk.message.common.exception.MessageErrorCode;
import com.shrona.mommytalk.message.common.exception.MessageException;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageLogQueryRepository;
import com.shrona.mommytalk.message.presentation.dtos.request.ReserveMessageRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.AvailableDateResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageLogDetailResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageLogInfoResponseDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageLogResponseDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/channels/{channelId}/messages")
@RestController
public class MessageLogRestController {

    private final MessageService messageService;
    private final MessageLogDetailService messageLogDetailService;
    private final MessageContentService messageContentService;
    private final ChannelService channelService;
    private final MessageLogQueryRepository messageLogQueryRepository;

    /**
     * 메시지 목록 조회
     */
    @GetMapping
    public ApiResponse<PageResponseDto<MessageLogResponseDto>> findMessageLogs(
        @PathVariable("channelId") Long channelId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {

        // 페이징 설정 (기본 20개, createdAt 내림차순은 QueryDSL에서 처리)
        Pageable pageable = PageRequest.of(page, size);

        // QueryDSL을 사용한 복잡한 쿼리로 MessageLog 목록 조회
        Page<MessageLogResponseDto> messageLogPage = messageLogQueryRepository
            .findMessageLogsByChannel(channelId, pageable);

        return ApiResponse.success(PageResponseDto.from(
            messageLogPage.toList(),
            messageLogPage.getNumber(),
            messageLogPage.getSize(),
            messageLogPage.getTotalElements(),
            messageLogPage.getTotalPages()
        ));
    }

    /**
     * MessageLog의 상세 정보를 반환
     */
    @GetMapping("/{messageLogId}")
    public ApiResponse<MessageLogInfoResponseDto> getMessageLogInfo(
        @PathVariable("channelId") Long channelId,
        @PathVariable("messageLogId") Long messageLogId
    ) {
        MessageLog messageLogInfo = messageService.findInfoByMessageId(messageLogId);

        Map<String, String> contentMap = messageContentService.groupMessageTextByLevel(
            messageLogInfo.getMessageType());

        return ApiResponse.success(
            MessageLogInfoResponseDto.of(
                messageLogInfo, contentMap
            )
        );
    }

    /**
     * MessageLog 상세 페이지에서 MessageLogDetail 목록을 반환
     */
    @GetMapping("/{messageLogId}/details")
    public ApiResponse<PageResponseDto<MessageLogDetailResponseDto>> getMessageLogDetailList(
        @PathVariable("channelId") Long channelId,
        @PathVariable("messageLogId") Long messageLogId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(ChannelErrorCode.CHANNEL_NOT_FOUND));

        Page<MessageLogDetail> logDetailList = messageLogDetailService.findLogDetailListByLogId(
            messageLogId, PageRequest.of(page, size));

        return ApiResponse.success(
            PageResponseDto.from(
                logDetailList.stream()
                    .map(logDetail -> MessageLogDetailResponseDto.of(channel, logDetail)).toList(),
                logDetailList.getNumber(),
                logDetailList.getSize(),
                logDetailList.getTotalElements(),
                logDetailList.getTotalPages()
            )
        );
    }

    /**
     * 사용 가능한 날짜 조회
     */
    @GetMapping("/available-dates")
    public ApiResponse<List<AvailableDateResponseDto>> getAvailableDate(
        @PathVariable("channelId") Long channelId) {

        // 1. 채널 정보 조회
        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(ChannelErrorCode.CHANNEL_NOT_FOUND));

        // 2. 오늘부터 14일 후까지 기간 설정
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(14);

        // 3. 9개 컨텐츠가 모두 승인된 MessageType 조회 (messageCount 포함)
        List<AvailableDateResponseDto> response = messageLogQueryRepository
            .findAvailableMessageTypesWithFullApprovedContent(channel, today, endDate);

        return ApiResponse.success(response);
    }

    @PostMapping("/schedule")
    public void reserveMessage(
        @PathVariable("channelId") Long channelId,
        @RequestBody ReserveMessageRequestDto requestDto
    ) {
        ZonedDateTime serverDateTime = requestDto.deliveryTime()
            .withZoneSameInstant(ZoneId.systemDefault());
        LocalDateTime localDateTime = serverDateTime.toLocalDateTime();

        log.info("[입력 데이터] {} , {}", requestDto, localDateTime);

        Optional<Channel> channelInfo = channelService.findChannelById(channelId);
        // 채널정보가 없는 경우 그냥 홈으로 보낸다.
        if (channelInfo.isEmpty()) {
            throw new ChannelException(ChannelErrorCode.CHANNEL_NOT_FOUND);
        }

        // 전송이 특정 그룹인 경우
        if (requestDto.messageTarget().equalsIgnoreCase(GROUP.getType())) {
            // 그룹 타겟 전송인데 그룹이 비어있는 경우
            if (requestDto.includeGroup() == null || requestDto.includeGroup().isEmpty()) {
                return;
            }

            messageService.createMessageSelectGroup(
                channelInfo.get(),
                requestDto.includeGroup(),
                requestDto.excludeGroup(),
                localDateTime,
                "");
        }
        // 전송이 전체 인 경우
        else if (requestDto.messageTarget().equalsIgnoreCase(ALL.getType())) {
            messageService.createMessageAllGroup(
                channelInfo.get(),
                requestDto.excludeGroup(),
                localDateTime,
                "");
        } else {
            throw new MessageException(MessageErrorCode.BAD_REQUEST);
        }

    }

    @DeleteMapping("/{messageIds}/cancel")
    public void cancelMessageLog() {

    }

}
