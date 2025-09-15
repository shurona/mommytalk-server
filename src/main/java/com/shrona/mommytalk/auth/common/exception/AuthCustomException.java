package com.shrona.mommytalk.auth.common.exception;

import lombok.Getter;

@Getter
public class AuthCustomException extends RuntimeException {

    private final AuthErrorCode code;

    public AuthCustomException(AuthErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }

}
