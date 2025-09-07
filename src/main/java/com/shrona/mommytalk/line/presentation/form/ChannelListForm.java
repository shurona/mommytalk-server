package com.shrona.mommytalk.line.presentation.form;

import com.shrona.mommytalk.channel.domain.Channel;

public record ChannelListForm(
    Long id,
    String name
) {

    public static ChannelListForm of(Channel channel) {
        return new ChannelListForm(
            channel.getId(),
            channel.getName()
        );
    }

}
