package com.shrona.mommytalk.openai.infrastructure.repository.dto;

import java.time.LocalDateTime;

public record PromptHistoryDto(
    Long promptId,
    String label,
    Boolean selected,
    LocalDateTime createdAt
) {

}
