package com.shrona.mommytalk.cloudflare.common.exception;

import lombok.Getter;

@Getter
public class CloudflareException extends RuntimeException {

    private final CloudflareErrorCode errorCode;

    public CloudflareException(CloudflareErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CloudflareException(CloudflareErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

}
