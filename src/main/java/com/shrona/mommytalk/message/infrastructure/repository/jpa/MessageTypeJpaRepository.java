package com.shrona.mommytalk.message.infrastructure.repository.jpa;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.domain.MessageType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageTypeJpaRepository extends JpaRepository<MessageType, Long> {

    Optional<MessageType> findByTheme(String title);

    /**
     * 선택된 날짜에 생성된 DeliveryTime 조회 (MessageContent fetch join)
     */
    @Query(
        "SELECT mt FROM MessageType mt LEFT JOIN FETCH mt.messageContentList WHERE mt.deliveryTime = :deliveryTime "
            + "and mt.channel = :channel")
    Optional<MessageType> findByDeliveryTime(LocalDate deliveryTime, Channel channel);

    /**
     * 채널과 배송일자로 MessageType 조회
     */
    Optional<MessageType> findByChannelAndDeliveryTime(Channel channel, LocalDate deliveryTime);

    /**
     * 채널ID와 배송일자로 MessageType 조회
     */
    Optional<MessageType> findByChannelIdAndDeliveryTime(Long channelId, LocalDate deliveryTime);

}
