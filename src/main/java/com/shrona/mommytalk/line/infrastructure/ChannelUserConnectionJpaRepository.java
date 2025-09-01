package com.shrona.mommytalk.line.infrastructure;

import com.shrona.mommytalk.line.domain.Channel;
import com.shrona.mommytalk.line.domain.ChannelUserConnection;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.line.infrastructure.dao.ChannelUserConnectionWithPhoneDao;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChannelUserConnectionJpaRepository extends JpaRepository<ChannelUserConnection, Long> {

    /**
     * 채널과 유저를 기준으로 연결 정보 단일 조회
     */
    Optional<ChannelUserConnection> findByChannelAndUser(Channel channel, User user);

    /**
     * 채널에 속한 유저 목록 조회
     */
    @Query(value = """
        SELECT new com.shrona.mommytalk.line.infrastructure.dao.ChannelUserConnectionWithPhoneDao(
                cuc.id,
                u.id,
                u.lineUser.lineId,
                u.phoneNumber,
                u.createdAt
            )
            FROM ChannelUserConnection cuc
            JOIN cuc.user u
            WHERE cuc.channel = :channel
        """)
    Page<ChannelUserConnectionWithPhoneDao> findAllByChannel(Channel channel, Pageable pageable);

    /**
     * ChannelUserConnection을 기준으로 휴대전화가 일치하는 유저 정보를 조회
     */
    @Query("""
            SELECT new com.shrona.mommytalk.line.infrastructure.dao.ChannelUserConnectionWithPhoneDao(
                cuc.id,
                u.id,
                u.lineUser.lineId,
                u.phoneNumber,
                u.createdAt
            )
            FROM ChannelUserConnection cuc
            JOIN cuc.user u
            WHERE cuc.channel = :channel
            AND (:query IS NULL OR u.phoneNumber.phoneNumber LIKE %:query%)
        """)
    Page<ChannelUserConnectionWithPhoneDao> findAllByChannelAndPhoneNumberWithUser(
        Channel channel, String query, Pageable pageable
    );
}