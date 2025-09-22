package com.shrona.mommytalk.message.infrastructure.repository;

import com.shrona.mommytalk.message.domain.ScheduledMessageText;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduledMessageTextJpaRepository
    extends JpaRepository<ScheduledMessageText, Long> {

}
