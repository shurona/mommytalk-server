package com.shrona.mommytalk.channel.domain;

import lombok.Getter;

@Getter
public enum ChannelPlatform {
    LINE("LINE"),
    KAKAO("KAKAO");

    private final String platform;

    ChannelPlatform(String platform) {
        this.platform = platform;
    }
}
