package com.shrona.mommytalk.group.common.exception;

import lombok.Getter;

@Getter
public class GroupException extends RuntimeException {

    private final GroupErrorCode groupErrorCode;

    public GroupException(GroupErrorCode code) {
        super(code.getMessage());
        this.groupErrorCode = code;
    }
}
