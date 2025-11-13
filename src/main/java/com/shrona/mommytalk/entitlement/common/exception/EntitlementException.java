package com.shrona.mommytalk.entitlement.common.exception;

import lombok.Getter;

@Getter
public class EntitlementException extends RuntimeException {

    private final EntitlementErrorCode code;

    public EntitlementException(EntitlementErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }
}
