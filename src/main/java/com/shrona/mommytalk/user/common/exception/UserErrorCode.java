package com.shrona.mommytalk.user.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum UserErrorCode {

    LOGIN_ERROR(HttpStatus.UNAUTHORIZED, "로그인 정보가 잘못되었습니다."),
    JWT_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "잘못된 JWT 토큰 정보입니다."),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "유저 정보가 없습니다."),


    DUPLICATE_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "중복 휴대전화 번호 입력입니다."),
    INVALID_PHONE_NUMBER_INPUT(HttpStatus.BAD_REQUEST, "잘못된 휴대전화 입력입니다");


    private HttpStatus status;
    private String message;
}
