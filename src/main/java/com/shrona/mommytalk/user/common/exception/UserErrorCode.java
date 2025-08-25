package com.shrona.mommytalk.user.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum UserErrorCode {

    INVALID_PHONE_NUMBER_INPUT(HttpStatus.BAD_REQUEST, "잘못된 휴대전화 입력입니다");

    private HttpStatus status;
    private String message;
}
