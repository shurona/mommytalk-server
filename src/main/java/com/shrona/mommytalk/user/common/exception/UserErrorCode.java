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
    DAILY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "오늘은 이미 문장을 생성하셨습니다. 내일 다시 시도해주세요"),
    NO_ACTIVE_ENTITLEMENT(HttpStatus.FORBIDDEN,
        "마미톡잉글리시 프로그램 구매 시 사용이 가능해요. 마미톡잉글리시 홈페이지에서 프로그램을 구매해 주세요"),
    GENERATE_SENTENCE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "문장 생성 중 오류가 발생하였습니다."),

    DUPLICATE_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "중복 휴대전화 번호 입력입니다."),
    INVALID_PHONE_NUMBER_INPUT(HttpStatus.BAD_REQUEST, "잘못된 휴대전화 입력입니다"),

    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "로그인 중 에러가 발생하였습니다. 고객센터에 문의해주세요");

    private HttpStatus status;
    private String message;
}
