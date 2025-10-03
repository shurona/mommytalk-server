package com.shrona.mommytalk.message.infrastructure.repository.jpa;

import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageContentJpaRepository
    extends JpaRepository<MessageContent, Long> {

    /**
     * MessageType, childLevel, userLevel로 MessageContent 조회
     */
    Optional<MessageContent> findByMessageTypeAndChildLevelAndUserLevel(
        MessageType messageType, Integer childLevel, Integer userLevel);

    /**
     * 특정 MessageType의 전체 MessageContent 개수 조회
     */
    int countByMessageType(MessageType messageType);

    /**
     * 특정 MessageType의 승인된 MessageContent 개수 조회
     */
    int countByMessageTypeAndApprovedTrue(MessageType messageType);

    /**
     * 특정 MessageType의 승인된 MessageContent 목록 조회
     */
    List<MessageContent> findByMessageTypeAndApprovedTrue(MessageType messageType);
}