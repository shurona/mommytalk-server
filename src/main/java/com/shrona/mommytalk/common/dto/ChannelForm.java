package com.shrona.mommytalk.common.dto;

import com.shrona.mommytalk.channel.domain.Channel;

public record ChannelForm(
    Long id,
    String name

) {

    public static ChannelForm of(Channel channel) {
        return new ChannelForm(
            channel.getId(),
            channel.getName()
        );
    }
}
