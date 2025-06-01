package com.shrona.line_demo.line.presentation.form;

import com.shrona.line_demo.line.domain.Channel;

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
