package com.shrona.line_demo.line.infrastructure;

import com.shrona.line_demo.line.domain.MessageLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageLogJpaRepository extends JpaRepository<MessageLog, Long> {

    @Query("select log from MessageLog log where log.reserveTime <= :now and log.status = 'PREPARE'")
    List<MessageLog> findAllByReservedMessage(LocalDateTime now);

}
