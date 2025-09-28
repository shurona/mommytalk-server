package com.shrona.mommytalk.message.presentation.dtos.request;

import java.time.LocalDate;

public record AiGenerateRequestDto(
    String theme,
    String context,
    LocalDate deliveryDate,
    Integer childLevel,
    Integer userLevel,
    Boolean regenerate,
    String language
) {

}
