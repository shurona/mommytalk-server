package com.shrona.mommytalk.openai.presentation.dtos.response;

import com.shrona.mommytalk.openai.infrastructure.repository.dto.PromptHistoryDto;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record PromptHistoryResponseDto(
    Long id,
    String label,
    Boolean selected,
    LocalDateTime createdAt
) {

    public static PromptHistoryResponseDto of(PromptHistoryDto historyDto) {
        return PromptHistoryResponseDto.builder()
            .id(historyDto.promptId())
            .label(historyDto.label())
            .selected(historyDto.selected())
            .createdAt(historyDto.createdAt())
            .build();
    }
}
