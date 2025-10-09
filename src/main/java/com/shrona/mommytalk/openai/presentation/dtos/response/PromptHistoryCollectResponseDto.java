package com.shrona.mommytalk.openai.presentation.dtos.response;

import java.util.List;

public record PromptHistoryCollectResponseDto(
    List<PromptHistoryResponseDto> basicPrompt,
    List<PromptHistoryResponseDto> advancePrompt
) {

    public static PromptHistoryCollectResponseDto of(
        List<PromptHistoryResponseDto> basic, List<PromptHistoryResponseDto> ad) {
        return new PromptHistoryCollectResponseDto(basic, ad);
    }

}
