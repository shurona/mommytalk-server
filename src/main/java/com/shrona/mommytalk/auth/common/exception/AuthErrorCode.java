package com.shrona.mommytalk.auth.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum AuthErrorCode {
    TOKEN_EXCHANGE_FAIL(HttpStatus.BAD_REQUEST, "토큰 조회 실패"),
    PROFILE_RETRIEVAL_FAILED(HttpStatus.BAD_REQUEST, "라인 유저 프로필 조회 실패"),
    ;

    private HttpStatus status;
    private String message;
}
