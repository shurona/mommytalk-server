package com.shrona.mommytalk.message.infrastructure.repository;

import com.shrona.mommytalk.message.domain.MessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageTemplateJpaRepository
    extends JpaRepository<MessageTemplate, Long> {

}
