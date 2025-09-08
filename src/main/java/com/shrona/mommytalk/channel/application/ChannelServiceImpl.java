package com.shrona.mommytalk.channel.application;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.line.infrastructure.ChannelJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ChannelServiceImpl implements ChannelService {

    private final ChannelJpaRepository channelRepository;

    @Override
    public List<Channel> findChannelList() {

        PageRequest pageInfo
            = PageRequest.of(0, 100, Sort.by(Order.asc("id")));

        return channelRepository.findAll(pageInfo).toList();
    }

    @Override
    public Optional<Channel> findChannelById(Long id) {
        return channelRepository.findById(id);
    }

    @Transactional
    public Channel updateInviteMessage(Long channelId, String message) {
        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(
                () -> new IllegalArgumentException("Channel not found with id: " + channelId));
        channel.updateInviteMessage(message);
        return channel;
    }
}
