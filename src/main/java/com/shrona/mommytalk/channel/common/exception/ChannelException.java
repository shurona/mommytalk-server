package com.shrona.mommytalk.channel.common.exception;

import lombok.Getter;

@Getter
public class ChannelException extends RuntimeException {

    private final ChannelErrorCode code;

    public ChannelException(ChannelErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }

}
