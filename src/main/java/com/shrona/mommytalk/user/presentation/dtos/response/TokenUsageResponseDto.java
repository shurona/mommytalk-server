package com.shrona.mommytalk.user.presentation.dtos.response;

public record TokenUsageResponseDto(
    Long totalCompletionTokens,
    Long totalTokens
) {
    public static TokenUsageResponseDto of(Long totalCompletionTokens, Long totalTokens) {
        return new TokenUsageResponseDto(
            totalCompletionTokens != null ? totalCompletionTokens : 0L,
            totalTokens != null ? totalTokens : 0L
        );
    }
}
