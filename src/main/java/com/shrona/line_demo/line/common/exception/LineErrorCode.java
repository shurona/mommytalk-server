package com.shrona.line_demo.line.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum LineErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    LINEUSER_NOT_FOUND(HttpStatus.BAD_REQUEST, "없은 라인유저 정보 입니다."),
    DUPLICATE_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "중복 휴대전화 번호 입력입니다."),
    INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "잘못된 휴대전화 입력입니다."),
    ;

    private HttpStatus status;
    private String message;

}
