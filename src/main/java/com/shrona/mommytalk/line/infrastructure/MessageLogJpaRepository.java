package com.shrona.mommytalk.line.infrastructure;

import com.shrona.mommytalk.line.domain.Channel;
import com.shrona.mommytalk.line.domain.MessageLog;
import com.shrona.mommytalk.line.infrastructure.dao.LogLineIdCount;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageLogJpaRepository extends JpaRepository<MessageLog, Long> {

    /**
     * 준비 상태인 현재 시간보다 이전인 모든 메시지를 조회한다
     */
    @Query("select log from MessageLog log where log.reserveTime <= :now and log.status = 'PREPARE'")
    List<MessageLog> findAllByReservedMessageBeforeNow(LocalDateTime now);

    /**
     * 준비 상태인 현재 시간보다 이전인 모든 메시지를 조회한다
     */
    @Query("select log from MessageLog log where log.status = 'PREPARE'")
    List<MessageLog> findAllByReservedMessage();

    /**
     * 준비 상태인 현재 시간보다 이전인 채널에 속한 메시지를 조회한다
     */
    @Query("select log from MessageLog log where log.reserveTime <= :now and log.status = 'PREPARE' and channel = :channel")
    List<MessageLog> findAllReservedMessageByChannel(Channel channel, LocalDateTime now);

    /**
     * 채널을 기준으로 페이지 목록 조회
     */
    Page<MessageLog> findAllByChannel(Channel channel, Pageable pageable);

    @Query(
        "SELECT new com.shrona.mommytalk.line.infrastructure.dao.LogLineIdCount(m.id, COUNT(ml)) " +
            "FROM MessageLog m LEFT JOIN m.messageLogLineInfoList ml " +
            "where m.id in :ids " +
            "GROUP BY m.id")
    List<LogLineIdCount> findLineCountPerLog(List<Long> ids);

}
