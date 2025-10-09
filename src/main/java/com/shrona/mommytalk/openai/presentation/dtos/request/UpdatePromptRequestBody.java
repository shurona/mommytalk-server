package com.shrona.mommytalk.openai.presentation.dtos.request;

public record UpdatePromptRequestBody(
    Long promptId,
    String prompt,
    String label
) {

}
