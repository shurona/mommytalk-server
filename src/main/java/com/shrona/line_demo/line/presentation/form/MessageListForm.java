package com.shrona.line_demo.line.presentation.form;

import com.shrona.line_demo.line.domain.MessageLog;
import java.time.LocalDateTime;

public record MessageListForm(
    Long id,
    LocalDateTime sendDateTime,
    LocalDateTime createdAtTime,
    String content,
    int receiverCount,
    String status
) {

    public static MessageListForm of(MessageLog messageLog) {
        return new MessageListForm(
            messageLog.getId(),
            messageLog.getReserveTime(),
            messageLog.getCreatedAt(),
            messageLog.getContent(),
            100,
            messageLog.getStatus().getStatus()
        );
    }

}
