package com.shrona.mommytalk.message.presentation.dtos.response;

import static lombok.AccessLevel.PRIVATE;

import com.shrona.mommytalk.message.domain.MessageType;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;

@Builder(access = PRIVATE)
public record MessageTypeInfoResponseDto(
    Long id,
    String theme,
    String context,
    Map<String, String> contentInfo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

    public static MessageTypeInfoResponseDto of(
        MessageType messageType, Map<String, String> contentInfo) {
        return MessageTypeInfoResponseDto.builder()
            .id(messageType.getId())
            .theme(messageType.getTheme())
            .context(messageType.getContext())
            .contentInfo(contentInfo)
            .createdAt(messageType.getCreatedAt())
            .updatedAt(messageType.getUpdatedAt())
            .build();
    }

}
