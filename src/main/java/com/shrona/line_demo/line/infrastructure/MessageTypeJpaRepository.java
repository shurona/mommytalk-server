package com.shrona.line_demo.line.infrastructure;

import com.shrona.line_demo.line.domain.MessageType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageTypeJpaRepository extends JpaRepository<MessageType, Long> {

    Optional<MessageType> findByTitle(String title);
}
