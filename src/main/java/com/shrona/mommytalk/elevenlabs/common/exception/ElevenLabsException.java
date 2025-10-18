package com.shrona.mommytalk.elevenlabs.common.exception;

import lombok.Getter;

@Getter
public class ElevenLabsException extends RuntimeException {

    private final ElevenLabsErrorCode code;

    public ElevenLabsException(ElevenLabsErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }

}
