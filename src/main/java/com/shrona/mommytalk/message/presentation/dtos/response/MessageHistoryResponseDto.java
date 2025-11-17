package com.shrona.mommytalk.message.presentation.dtos.response;

import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.MessageType;
import java.time.LocalDate;

public record MessageHistoryResponseDto(
    Long typeId,
    String typeTheme,
    LocalDate deliveryTime,
    Long messageContentId,
    Long messageLogDetailId
) {

    public static MessageHistoryResponseDto of(
        MessageType messageType, MessageContent messageContent, MessageLogDetail detail) {
        return new MessageHistoryResponseDto(
            messageType.getId(),
            messageType.getTheme(),
            messageType.getDeliveryTime(),
            messageContent.getId(),
            detail.getId()
        );
    }
}
