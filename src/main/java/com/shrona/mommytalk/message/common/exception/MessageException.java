package com.shrona.mommytalk.message.common.exception;

import lombok.Getter;

@Getter
public class MessageException extends RuntimeException {

    private final MessageErrorCode code;

    public MessageException(MessageErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }

}
