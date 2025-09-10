package com.shrona.mommytalk.channel.presentation.dtos;

import com.shrona.mommytalk.channel.domain.Channel;

public record ChannelResponseDto(
    Long channelId,
    String channelName
) {

    public static ChannelResponseDto of(Channel channel) {
        return new ChannelResponseDto(
            channel.getId(),
            channel.getName()
        );
    }

}
