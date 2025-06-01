package com.shrona.line_demo.line.common.exception;

import lombok.Getter;

@Getter
public class LineException extends RuntimeException {

    private final LineErrorCode code;

    public LineException(LineErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }

}
