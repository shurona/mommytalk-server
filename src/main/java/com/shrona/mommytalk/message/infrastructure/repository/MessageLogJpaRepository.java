package com.shrona.mommytalk.message.infrastructure.repository;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.line.infrastructure.dao.LogMessageIdCount;
import com.shrona.mommytalk.message.domain.MessageLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageLogJpaRepository extends JpaRepository<MessageLog, Long> {

    /**
     * 현재 시간보다 이전인 모든 메시지를 조회한다
     */
    @Query("select log from MessageLog log where log.reserveTime <= :now")
    List<MessageLog> findAllByBeforeNow(LocalDateTime now);

    /**
     * 현재 시간보다 이전인 채널에 속한 메시지를 조회한다
     */
    @Query("select log from MessageLog log where log.reserveTime <= :now and channel = :channel")
    List<MessageLog> findAllReservedMessageByChannel(Channel channel, LocalDateTime now);

    /**
     * 채널을 기준으로 페이지 목록 조회
     */
    Page<MessageLog> findAllByChannel(Channel channel, Pageable pageable);

    @Query(
        "SELECT new com.shrona.mommytalk.line.infrastructure.dao.LogMessageIdCount(m.id, COUNT(ml)) "
            +
            "FROM MessageLog m LEFT JOIN m.messageLogDetailList ml " +
            "where m.id in :ids " +
            "GROUP BY m.id")
    List<LogMessageIdCount> findMessageCountPerLog(List<Long> ids);

}
