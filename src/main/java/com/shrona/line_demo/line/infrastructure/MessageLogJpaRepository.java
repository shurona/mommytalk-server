package com.shrona.line_demo.line.infrastructure;

import com.shrona.line_demo.line.domain.MessageLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageLogJpaRepository extends JpaRepository<MessageLog, Long> {

}
