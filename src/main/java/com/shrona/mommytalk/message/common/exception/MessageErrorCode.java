package com.shrona.mommytalk.message.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum MessageErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
    MESSAGE_PROMPT_NOT_EXIST(HttpStatus.BAD_REQUEST, "현재 메시지 프롬프트가 존재하지 않습니다.."),
    REGISTERED_MESSAGE_PROMPT(HttpStatus.BAD_REQUEST, "등록된 메시지 프롬프트는 삭제할 수 없습니다."),

    MESSAGE_NOT_SCHEDULED_FOR_DATE(HttpStatus.BAD_REQUEST, "해당 날짜에 메시지가 예약되어 있지 않았습니다."),
    MESSAGE_TYPE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "해당 날짜에 메시지 타입이 이미 존재합니다."),
    MESSAGE_CONTENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "해당 레벨의 메시지 컨텐츠가 이미 존재합니다."),
    MESSAGE_NOT_DELIVER_YET(HttpStatus.BAD_REQUEST, "메시지가 아직 발송되지 않았습니다."),
    MESSAGE_CHANNEL_MISMATCH(HttpStatus.BAD_REQUEST, "메시지와 채널 정보 매칭이 잘못되었습니다."),
    MESSAGE_ALREADY_CANCEL(HttpStatus.BAD_REQUEST, "메시지가 이미 취소되었습니다."),
    MESSAGE_CANCEL_TOO_LATE(HttpStatus.BAD_REQUEST, "예약 시간 30분 전에는 취소할 수 없습니다."),
    NEED_MORE_DATE_FOR_APPROVED(HttpStatus.BAD_REQUEST, "승인 되기에 데이터가 부족합니다."),

    MESSAGE_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지 정보를 찾을 수 없습니다."),
    MESSAGE_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지 컨텐츠를 찾을 수 없습니다."),
    MESSAGE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지 타입을 찾을 수 없습니다."),

    MESSAGE_CONTENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 채널의 메시지 컨텐츠에 접근할 수 없습니다."),

    ;

    private HttpStatus status;
    private String message;
}
