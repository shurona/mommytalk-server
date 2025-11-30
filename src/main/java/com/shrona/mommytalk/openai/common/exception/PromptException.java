package com.shrona.mommytalk.openai.common.exception;

import lombok.Getter;

@Getter
public class PromptException extends RuntimeException {

    private final PromptErrorCode code;

    public PromptException(PromptErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }

}
