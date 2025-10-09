package com.shrona.mommytalk.openai.presentation.dtos.request;

import com.shrona.mommytalk.openai.domain.type.PromptType;

public record InsertPromptRequestBody(
    String label,
    String prompt,
    PromptType type
) {

}
