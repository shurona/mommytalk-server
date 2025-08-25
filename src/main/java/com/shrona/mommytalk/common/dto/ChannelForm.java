package com.shrona.mommytalk.common.dto;

import com.shrona.mommytalk.line.domain.Channel;

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
