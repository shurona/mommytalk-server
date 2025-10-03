package com.shrona.mommytalk.message.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum MessageErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),

    MESSAGE_PROMPT_NOT_EXIST(HttpStatus.BAD_REQUEST, "현재 메시지 프롬프트가 존재하지 않습니다.."),

    MESSAGE_NOT_SCHEDULED_FOR_DATE(HttpStatus.BAD_REQUEST, "해당 날짜에 메시지가 예약되어 있지 않았습니다."),

    MESSAGE_CONTENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "해당 레벨의 메시지 컨텐츠가 이미 존재합니다."),

    MESSAGE_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지 컨텐츠를 찾을 수 없습니다."),

    MESSAGE_CONTENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 채널의 메시지 컨텐츠에 접근할 수 없습니다."),

    MESSAGE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지 타입을 찾을 수 없습니다."),
    ;

    private HttpStatus status;
    private String message;
}
