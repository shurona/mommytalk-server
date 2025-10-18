package com.shrona.mommytalk.cloudflare.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CloudflareErrorCode {

    UPLOAD_FAILED("파일 업로드에 실패했습니다"),
    FILE_NOT_FOUND("파일을 찾을 수 없습니다"),
    INVALID_FILE("유효하지 않은 파일입니다"),
    S3_CLIENT_ERROR("S3 클라이언트 오류가 발생했습니다"),
    IO_ERROR("파일 입출력 오류가 발생했습니다");

    private final String message;

}
