package com.shrona.mommytalk.message.presentation.dtos.request;

import java.util.List;

public record UpdateAudioTextsRequestDto(
    List<AudioTextItem> items
) {

    public record AudioTextItem(
        Integer userLevel,
        Integer childLevel,
        String momAudioText,
        String childAudioText
    ) {

    }
}
