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
    Integer messageCount,
    Integer successCount,
    Integer failCount,
    Integer totalCount
) {

    public static MessageLogResponseDto of(
        Long id,
        String theme,
        String status,
        LocalDateTime createdAt,
        LocalDateTime deliveryDate,
        Integer messageCount,
        Integer successCount,
        Integer failCount,
        Integer totalCount
    ) {
        return MessageLogResponseDto.builder()
            .id(id)
            .theme(theme)
            .status(status)
            .createdAt(createdAt.plusHours(9))
            .deliveryDate(deliveryDate.plusHours(9))
            .messageCount(messageCount)
            .successCount(successCount)
            .failCount(failCount)
            .totalCount(totalCount)
            .build();
    }
}