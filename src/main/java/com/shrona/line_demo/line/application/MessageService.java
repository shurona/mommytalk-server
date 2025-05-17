package com.shrona.line_demo.line.application;

import com.shrona.line_demo.line.domain.MessageLog;
import com.shrona.line_demo.line.domain.MessageType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {

    MessageType createMessageType(String title, String text);

    List<MessageLog> createMessage
        (Long messageTypeId, List<Long> groupId, LocalDateTime reserveTime, String content);

    MessageLog findByMessageId(Long id);

    Page<MessageLog> findMessageLogList(Pageable pageable);
}
