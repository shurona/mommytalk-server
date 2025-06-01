package com.shrona.line_demo.line.infrastructure;

import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.ChannelLineUser;
import com.shrona.line_demo.line.domain.LineUser;
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
    @Query(value = "select clu from ChannelLineUser clu left join fetch clu.lineUser lu where clu.channel = :channel")
    Page<ChannelLineUser> findAllByChannel(Channel channel, Pageable pageable);

    @Query("""
            SELECT clu
            FROM ChannelLineUser clu
            JOIN clu.lineUser lu
            WHERE clu.channel = :channel
            AND CAST(lu.phoneNumber AS string) LIKE %:query%
        """)
    Page<ChannelLineUser> findAllByChannelAndPhoneNumber(
        Channel channel, String query, Pageable pageable
    );
}
