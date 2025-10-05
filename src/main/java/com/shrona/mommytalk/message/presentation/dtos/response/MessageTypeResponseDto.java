package com.shrona.mommytalk.message.presentation.dtos.response;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record MessageTypeResponseDto(
    Long id,
    LocalDate localDate,
    String theme,
    String context
) {

    public static MessageTypeResponseDto of(
        Long id, LocalDate localDate, String theme, String context) {
        return MessageTypeResponseDto.builder()
            .localDate(localDate)
            .id(id)
            .theme(theme)
            .context(context)
            .build();
    }
}