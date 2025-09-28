package com.shrona.mommytalk.message.presentation.dtos.request;

public record UpdateTemplateRequestDto(
    String messageText,
    String vocaUrl,
    String diaryUrl
) {

}
