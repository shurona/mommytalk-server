package com.shrona.mommytalk.line.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {

    /**
     * 메시지 타입 생성
     */
    public MessageType createMessageType(String title, String text);

    /**
     * 선택된 그룹에 메시지 전송(제외 그룹 확인)
     */
    public List<MessageLog> createMessageSelectGroup
    (Channel channel, Long messageTypeId,
        List<Long> selectedGroupIds, List<Long> selectedExGroupIds,
        LocalDateTime reserveTime, String content);

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

    /**
     * 로그의 라인 아이디 갯수를 갖고 온다.
     */
    public Map<Long, Integer> findLineIdCountByLog(List<Long> list);

    /**
     * 메시지 내용 변경
     */
    public MessageLog cancelSendMessage(Long messageId);

    /**
     * 메시지 내용 변경
     */
    public MessageLog updateMessageLog(Long messageId, String content);
}
