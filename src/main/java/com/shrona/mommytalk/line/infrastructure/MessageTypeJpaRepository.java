package com.shrona.mommytalk.line.infrastructure;

import com.shrona.mommytalk.line.domain.MessageType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageTypeJpaRepository extends JpaRepository<MessageType, Long> {

    Optional<MessageType> findByTitle(String title);
}
