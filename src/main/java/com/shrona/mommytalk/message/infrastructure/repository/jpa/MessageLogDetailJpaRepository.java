package com.shrona.mommytalk.message.infrastructure.repository.jpa;

import com.shrona.mommytalk.message.domain.MessageLogDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageLogDetailJpaRepository extends JpaRepository<MessageLogDetail, Long> {

}
