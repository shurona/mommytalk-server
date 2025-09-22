package com.shrona.mommytalk.message.infrastructure.repository;

import com.shrona.mommytalk.message.domain.MessageType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageTypeJpaRepository extends JpaRepository<MessageType, Long> {

    Optional<MessageType> findByTitle(String title);

    /**
     *
     */
    Optional<MessageType> findByDeliveryTime(LocalDate deliveryTime);
}
