package com.shrona.mommytalk.line.infrastructure;

import com.shrona.mommytalk.line.domain.Channel;
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
     * 채널과 라인 유저를 기준으로 유저 단일 조회
     */
    Optional<ChannelLineUser> findByChannelAndLineUser(Channel channel, LineUser lineUser);

    /**
     * 채널에 속한 유저 목록 조회
     */
    @Query(value = """
        SELECT new com.shrona.mommytalk.line.infrastructure.dao.ChannelLineUserWithPhoneDao(
                clu.id,
                lu.id,
                lu.lineId,
                u.phoneNumber,
                lu.createdAt
            )
            FROM ChannelLineUser clu
            JOIN clu.lineUser lu
            LEFT JOIN User u ON u.lineUser = lu
            WHERE clu.channel = :channel
        """)
    Page<ChannelLineUserWithPhoneDao> findAllByChannel(Channel channel, Pageable pageable);

    /**
     * ChannelLineUser를 기준으로 휴대전화가 알맞는 유저 정보를 갖고 온다.
     */
    @Query("""
            SELECT new com.shrona.mommytalk.line.infrastructure.dao.ChannelLineUserWithPhoneDao(
                clu.id,
                lu.id,
                lu.lineId,
                u.phoneNumber,
                lu.createdAt
            )
            FROM ChannelLineUser clu
            JOIN clu.lineUser lu
            LEFT JOIN User u ON u.lineUser = lu
            WHERE clu.channel = :channel
            AND (:query IS NULL OR u.phoneNumber.phoneNumber LIKE %:query%)
        """)
    Page<ChannelLineUserWithPhoneDao> findAllByChannelAndPhoneNumberWithUser(
        Channel channel, String query, Pageable pageable
    );
}
