package com.shrona.mommytalk.line.infrastructure.repository.jpa;


import com.shrona.mommytalk.channel.domain.Channel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelJpaRepository extends JpaRepository<Channel, Long> {

    Optional<Channel> findByChannelCode(String channelCode);
}
