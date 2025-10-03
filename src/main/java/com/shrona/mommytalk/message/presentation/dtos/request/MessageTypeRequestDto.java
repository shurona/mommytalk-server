package com.shrona.mommytalk.message.presentation.dtos.request;

import java.time.LocalDate;

public record MessageTypeRequestDto(
    LocalDate localDate,
    String theme,
    String context
) {
}