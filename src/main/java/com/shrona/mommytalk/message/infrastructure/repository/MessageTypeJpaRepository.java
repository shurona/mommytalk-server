package com.shrona.mommytalk.message.infrastructure.repository;

import com.shrona.mommytalk.message.domain.MessageType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageTypeJpaRepository extends JpaRepository<MessageType, Long> {

    Optional<MessageType> findByTitle(String title);

    /**
     * 선택된 날짜에 생성된 DeliveryTime 조회 (MessageTemplate fetch join)
     */
    @Query("SELECT mt FROM MessageType mt LEFT JOIN FETCH mt.messageTemplateList WHERE mt.deliveryTime = :deliveryTime")
    Optional<MessageType> findByDeliveryTime(LocalDate deliveryTime);
}
