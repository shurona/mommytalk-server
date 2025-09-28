package com.shrona.mommytalk.message.presentation.dtos.response;

import static lombok.AccessLevel.PRIVATE;

import lombok.Builder;

@Builder(access = PRIVATE)
public record AiGenerateResponseDto(
    String message,
    MessageContentData data
) {

    @Builder(access = PRIVATE)
    public record MessageContentData(
        MessageContentResponseDto content
    ) {

        public static MessageContentData of(MessageContentResponseDto content) {
            return MessageContentData.builder()
                .content(content)
                .build();
        }
    }

    public static AiGenerateResponseDto of(MessageContentResponseDto content) {
        return AiGenerateResponseDto.builder()
            .message("Success")
            .data(MessageContentData.of(content))
            .build();
    }
}