package com.shrona.mommytalk.message.infrastructure.repository.jpa;

import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageContentJpaRepository
    extends JpaRepository<MessageContent, Long> {

    /**
     * MessageType, childLevel, userLevel로 MessageContent 조회
     */
    Optional<MessageContent> findByMessageTypeAndChildLevelAndUserLevel(
        MessageType messageType, Integer childLevel, Integer userLevel);
}