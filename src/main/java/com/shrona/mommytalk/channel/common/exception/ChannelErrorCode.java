package com.shrona.mommytalk.channel.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ChannelErrorCode {
    CHANNEL_NOT_FOUND(HttpStatus.BAD_REQUEST, "유저 정보가 없습니다."),
    ;

    private HttpStatus status;
    private String message;
}
