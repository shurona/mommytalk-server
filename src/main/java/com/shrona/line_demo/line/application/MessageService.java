package com.shrona.line_demo.line.application;

import com.shrona.line_demo.line.domain.Channel;
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
    public MessageType createMessageType(String title, String text);

    /**
     * 메시지 생성(발송 시 사용)
     */
    public List<MessageLog> createMessageSelectGroup
    (Channel channel, Long messageTypeId, List<Long> groupId, LocalDateTime reserveTime,
        String content);

    /**
     * 모든 그룹에 메시지 전송(제외 그룹 확인)
     */
    public List<MessageLog> createMessageAllGroup
    (Channel channel, Long messageTypeId, List<Long> exceptGroupIds, LocalDateTime reserveTime,
        String content);

    /**
     * 메시지 단일 조회
     */
    public MessageLog findByMessageId(Long id);

    /**
     * 메시지 로그 조회
     */
    public Page<MessageLog> findMessageLogList(Channel channel, Pageable pageable);

    /**
     * 예약 된 메시지 목록 조회
     */
    public List<MessageLog> findReservedMessage(Channel channel);

    /**
     * 예약 된 메시지 목록 조회
     */
    public List<MessageLog> findReservedAllMessage();

}
