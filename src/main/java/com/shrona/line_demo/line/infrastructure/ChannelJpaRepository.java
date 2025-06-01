package com.shrona.line_demo.line.infrastructure;


import com.shrona.line_demo.line.domain.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelJpaRepository extends JpaRepository<Channel, Long> {

}
