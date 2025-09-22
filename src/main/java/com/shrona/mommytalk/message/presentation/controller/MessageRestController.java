package com.shrona.mommytalk.message.presentation.controller;

import static com.shrona.mommytalk.line.presentation.form.TargetType.ALL;
import static com.shrona.mommytalk.line.presentation.form.TargetType.GROUP;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelErrorCode;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.message.application.MessageService;
import com.shrona.mommytalk.message.application.MessageTypeService;
import com.shrona.mommytalk.message.presentation.dtos.request.ReserveMessageRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.AvailableDateResponseDto;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/channels/{channelId}/messages")
@RestController
public class MessageRestController {

    private final MessageService messageService;
    private final MessageTypeService messageTypeService;
    private final ChannelService channelService;

    @GetMapping("/available-dates")
    public ApiResponse<List<AvailableDateResponseDto>> getAvailableDate() {

        List<String> dates = List.of("2025-01-01", "2025-01-02", "2025-01-03");
        Random random = new Random();

        List<AvailableDateResponseDto> res = new ArrayList<>();
        for (String date : dates) {
            res.add(AvailableDateResponseDto.of(date, random.nextInt(5) + 1));
        }

        return ApiResponse.success(res);
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

        // 메시지 날짜로 메시지 타입 ID를 갖고온다.

        // 전송이 특정 그룹인 경우
        if (requestDto.messageTarget().equals(GROUP.getType())) {
            // 그룹 타겟 전송인데 그룹이 비어있는 경우
            if (requestDto.includeGroup() == null || requestDto.includeGroup().isEmpty()) {
//                throw new MessageEr();
                return;
            }
            messageService.createMessageSelectGroup(
                channelInfo.get(),
                1L, requestDto.includeGroup(), requestDto.excludeGroup(),
                localDateTime,
                "");
        }
        // 전송이 전체 인 경우
        else if (requestDto.messageTarget().equals(ALL.getType())) {
            messageService.createMessageAllGroup(
                channelInfo.get(), 1L, requestDto.excludeGroup(), localDateTime,
                "requestDto.content()");
        }

    }

    @DeleteMapping("/{messageIds}/cancel")
    public void cancelMessageLog() {

    }

}
