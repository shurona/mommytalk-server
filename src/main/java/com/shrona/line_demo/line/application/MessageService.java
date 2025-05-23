package com.shrona.line_demo.line.application;

import com.shrona.line_demo.line.domain.MessageLog;
import com.shrona.line_demo.line.domain.MessageType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {

    /**
     * 메시지 타입 생성
     */
    MessageType createMessageType(String title, String text);

    /**
     * 메시지 생성(발송 시 사용)
     */
    List<MessageLog> createMessage
    (Long messageTypeId, List<Long> groupId, LocalDateTime reserveTime, String content);

    /**
     * 메시지 단일 조회
     */
    MessageLog findByMessageId(Long id);

    /**
     * 메시지 로그 조회
     */
    Page<MessageLog> findMessageLogList(Pageable pageable);

    /**
     * 예약 된 메시지 목록 조회
     */
    List<MessageLog> findReservedMessage();

}
