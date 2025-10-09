package com.shrona.mommytalk.openai.presentation.dtos.response;

import static lombok.AccessLevel.PRIVATE;

import com.shrona.mommytalk.openai.domain.MessagePrompt;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder(access = PRIVATE)
public record PromptResponseDto(
    Long id,
    String label,
    String prompt,
    String type,
    LocalDateTime createdAt
) {

    public static PromptResponseDto of(MessagePrompt prompt) {
        return PromptResponseDto.builder()
            .id(prompt.getId())
            .label(prompt.getLabel())
            .prompt(prompt.getPrompt())
            .type(prompt.getType().getType())
            .createdAt(prompt.getCreatedAt())
            .build();
    }

}
