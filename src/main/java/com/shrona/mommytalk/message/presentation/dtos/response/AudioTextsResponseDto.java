package com.shrona.mommytalk.message.presentation.dtos.response;

import java.util.List;

public record AudioTextsResponseDto(
    Long messageTypeId,
    List<AudioTextItemDto> items
) {

    public record AudioTextItemDto(
        Long contentId,
        Integer userLevel,
        Integer childLevel,
        boolean contentExists,
        String messageText,
        String momAudioText,
        String childAudioText,
        String momAudioUrl,
        String childAudioUrl
    ) {

    }
}
