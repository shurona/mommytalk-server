package com.shrona.mommytalk.group.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum GroupErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "그룹 정보를 찾을 수 없습니다."),
    ;

    private HttpStatus status;
    private String message;
}
