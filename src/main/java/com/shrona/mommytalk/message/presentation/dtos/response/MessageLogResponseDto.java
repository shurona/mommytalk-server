package com.shrona.mommytalk.message.presentation.dtos.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record MessageLogResponseDto(
    Long id,
    String theme,
    String status,
    LocalDateTime createdAt,
    LocalDateTime deliveryDate,
    Integer messageCount
) {

    public static MessageLogResponseDto of(
        Long id,
        String theme,
        String status,
        LocalDateTime createdAt,
        LocalDateTime deliveryDate,
        Integer messageCount
    ) {
        return MessageLogResponseDto.builder()
            .id(id)
            .theme(theme)
            .status(status)
            .createdAt(createdAt)
            .deliveryDate(deliveryDate)
            .messageCount(messageCount)
            .build();
    }
}