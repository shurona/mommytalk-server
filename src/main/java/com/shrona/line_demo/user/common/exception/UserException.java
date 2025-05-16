package com.shrona.line_demo.user.common.exception;

public class UserException extends RuntimeException {

    private final UserErrorCode errorCode;

    public UserException(UserErrorCode code) {
        super(code.getMessage());
        this.errorCode = code;
    }
}
