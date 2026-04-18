package com.shrona.mommytalk.message.common.exception;

public class BatchValidationException extends RuntimeException {

    private final Object data;

    public BatchValidationException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public Object getData() {
        return data;
    }
}
