package com.shrona.mommytalk.message.presentation.dtos.response;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record MessageTypeResponseDto(
    LocalDate localDate,
    String theme,
    String context
) {

    public static MessageTypeResponseDto of(LocalDate localDate, String theme, String context) {
        return MessageTypeResponseDto.builder()
            .localDate(localDate)
            .theme(theme)
            .context(context)
            .build();
    }
}