package com.shrona.mommytalk.openai.presentation.dtos.response;

import com.shrona.mommytalk.openai.domain.MessagePrompt;
import java.time.LocalDateTime;

public record SelectedPromptResponseDto(
    Long id,
    String prompt,
    String type,
    LocalDateTime createdAt
) {

    public static SelectedPromptResponseDto of(MessagePrompt messagePrompt) {
        return new SelectedPromptResponseDto(
            messagePrompt.getId(),
            messagePrompt.getPrompt(),
            messagePrompt.getType().getType(),
            messagePrompt.getCreatedAt()
        );
    }

}
