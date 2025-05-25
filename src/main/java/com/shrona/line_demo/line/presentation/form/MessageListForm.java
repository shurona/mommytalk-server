package com.shrona.line_demo.line.presentation.form;

import com.shrona.line_demo.line.domain.MessageLog;
import java.time.LocalDateTime;
import java.util.Map;

public record MessageListForm(
    Long id,
    LocalDateTime sendDateTime,
    LocalDateTime createdAtTime,
    String content,
    int receiverCount,
    String status
) {

    public static MessageListForm of(MessageLog messageLog, Map<Long, Integer> groupUserCount) {
        return new MessageListForm(
            messageLog.getId(),
            messageLog.getReserveTime(),
            messageLog.getCreatedAt(),
            messageLog.getContent(),
            groupUserCount.getOrDefault(messageLog.getGroup().getId(), 0),
            messageLog.getStatus().getStatus()
        );
    }

}
