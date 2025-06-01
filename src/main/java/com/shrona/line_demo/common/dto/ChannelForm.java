package com.shrona.line_demo.common.dto;

import com.shrona.line_demo.line.domain.Channel;

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
