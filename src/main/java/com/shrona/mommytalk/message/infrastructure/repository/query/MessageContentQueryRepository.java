package com.shrona.mommytalk.message.infrastructure.repository.query;

import com.shrona.mommytalk.message.domain.MessageContent;

public interface MessageContentQueryRepository {

    MessageContent findById(Long id);

    MessageContent findByTypeAndUserLevel(Long typeId, Integer userLevel, Integer childLevel);
}
