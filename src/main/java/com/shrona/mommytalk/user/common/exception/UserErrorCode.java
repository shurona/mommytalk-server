package com.shrona.mommytalk.user.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum UserErrorCode {

    DUPLICATE_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "중복 휴대전화 번호 입력입니다."),
    INVALID_PHONE_NUMBER_INPUT(HttpStatus.BAD_REQUEST, "잘못된 휴대전화 입력입니다");


    private HttpStatus status;
    private String message;
}
