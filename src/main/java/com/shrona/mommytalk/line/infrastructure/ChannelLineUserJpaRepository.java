package com.shrona.mommytalk.line.infrastructure;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.line.domain.ChannelLineUser;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.dao.ChannelLineUserWithPhoneDao;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChannelLineUserJpaRepository extends JpaRepository<ChannelLineUser, Long> {

    /**
     * 채널과 유저를 기준으로 연결 정보 단일 조회
     */
    Optional<ChannelLineUser> findByChannelAndLineUser(Channel channel, LineUser lineUser);

    /**
     * 채널에 속한 유저 목록 조회
     */
    @Query(value = """
        SELECT new com.shrona.mommytalk.line.infrastructure.dao.ChannelLineUserWithPhoneDao(
                clu.id,
                clu.lineUser.user.id,
                clu.lineUser.lineId,
                clu.lineUser.user.phoneNumber,
                clu.lineUser.createdAt
            )
            FROM ChannelLineUser clu
            WHERE clu.channel = :channel
            AND clu.lineUser.user IS NOT NULL
        """)
    Page<ChannelLineUserWithPhoneDao> findAllByChannel(Channel channel, Pageable pageable);

    /**
     * ChannelLineUser를 기준으로 휴대전화가 일치하는 유저 정보를 조회
     */
    @Query("""
            SELECT new com.shrona.mommytalk.line.infrastructure.dao.ChannelLineUserWithPhoneDao(
                clu.id,
                clu.lineUser.user.id,
                clu.lineUser.lineId,
                clu.lineUser.user.phoneNumber,
                clu.lineUser.user.createdAt
            )
            FROM ChannelLineUser clu
            WHERE clu.channel = :channel
            AND clu.lineUser.user IS NOT NULL
            AND (:query IS NULL OR clu.lineUser.user.phoneNumber.phoneNumber LIKE %:query%)
        """)
    Page<ChannelLineUserWithPhoneDao> findAllByChannelAndPhoneNumberWithUser(
        Channel channel, String query, Pageable pageable
    );
}