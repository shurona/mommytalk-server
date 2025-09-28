package com.shrona.mommytalk.openai.infrastructure.repository;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.openai.domain.MessagePrompt;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessagePromptJpaRepository extends JpaRepository<MessagePrompt, Long> {

    Optional<MessagePrompt> findByChannel(Channel channel);

}

