package com.shrona.mommytalk.openai.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum PromptErrorCode {

    PROMPT_NOT_FOUND(HttpStatus.NOT_FOUND, "프롬프트를 찾을 수 없습니다."),
    MESSAGE_PROMPT_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지 프롬프트를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
