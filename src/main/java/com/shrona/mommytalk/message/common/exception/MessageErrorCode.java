package com.shrona.mommytalk.message.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum MessageErrorCode {


    MESSAGE_NOT_SCHEDULED_FOR_DATE(HttpStatus.BAD_REQUEST, "해당 날짜에 메시지가 예약되어 있지 않았습니다."),
    ;

    private HttpStatus status;
    private String message;
}
