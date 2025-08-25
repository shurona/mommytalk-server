package com.shrona.mommytalk.line.presentation.form;

import com.shrona.mommytalk.line.domain.MessageLog;
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

    public static MessageListForm of(MessageLog messageLog, Map<Long, Integer> logLineIdCount) {
        return new MessageListForm(
            messageLog.getId(),
            messageLog.getReserveTime().plusHours(9), // TODO: 서버는 utc 사용하고 클라이언트에서 반영하도록 변경
            messageLog.getCreatedAt().plusHours(9),
            messageLog.getContent(),
            logLineIdCount.getOrDefault(messageLog.getId(), 0),
            messageLog.getStatus().getStatus()
        );
    }

}
