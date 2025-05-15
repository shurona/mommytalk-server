package com.shrona.line_demo.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum LineErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    ;

    private HttpStatus status;
    private String message;

}
