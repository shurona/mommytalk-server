package com.shrona.mommytalk.elevenlabs.infrastructure.reposiotry;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.elevenlabs.domain.VoicePreset;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoicePresetRepository extends JpaRepository<VoicePreset, Long> {

    List<VoicePreset> findByChannelOrderBySortOrderAsc(Channel channel);

    Optional<VoicePreset> findByIdAndChannel(Long id, Channel channel);

    Optional<VoicePreset> findByChannelAndVoiceId(Channel channel, String voiceId);

    Optional<VoicePreset> findByChannelAndDefaultForMommyTrue(Channel channel);

    Optional<VoicePreset> findByChannelAndDefaultForChildTrue(Channel channel);

}
