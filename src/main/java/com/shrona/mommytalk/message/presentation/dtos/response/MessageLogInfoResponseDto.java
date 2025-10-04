package com.shrona.mommytalk.message.presentation.dtos.response;

import static lombok.AccessLevel.PRIVATE;

import com.shrona.mommytalk.message.domain.MessageLog;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;

@Builder(access = PRIVATE)
public record MessageLogInfoResponseDto(
    Long id,
    String theme,
    String context,
    String deliveryDate,
    String status,
    Map<String, String> contentInfo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {


    public static MessageLogInfoResponseDto of(MessageLog messageLog,
        Map<String, String> contentInfo) {
        return MessageLogInfoResponseDto.builder()
            .id(messageLog.getId())
            .theme(messageLog.getMessageType().getTheme())
            .context(messageLog.getMessageType().getContext())
            .deliveryDate(messageLog.getReserveTime().toString())
            .contentInfo(contentInfo)
            .createdAt(messageLog.getCreatedAt())
            .updatedAt(messageLog.getUpdatedAt())
            .build();
    }

}
