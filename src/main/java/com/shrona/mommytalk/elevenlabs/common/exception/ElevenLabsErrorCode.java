package com.shrona.mommytalk.elevenlabs.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ElevenLabsErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_TEXT(HttpStatus.BAD_REQUEST, "변환할 텍스트가 비어있습니다."),
    INVALID_MESSAGE_CONTENT_ID(HttpStatus.BAD_REQUEST, "메시지 컨텐츠 ID가 유효하지 않습니다."),

    API_CALL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ElevenLabs API 호출에 실패했습니다."),
    EMPTY_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "ElevenLabs API 응답이 비어있습니다."),
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "오디오 파일 저장에 실패했습니다."),
    DIRECTORY_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "디렉토리 생성에 실패했습니다."),

    AUDIO_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "오디오 생성에 실패했습니다."),

    VOICE_PRESET_NOT_FOUND(HttpStatus.NOT_FOUND, "음성 프리셋을 찾을 수 없습니다."),
    VOICE_PRESET_ORDER_MISMATCH(HttpStatus.BAD_REQUEST, "순서 변경 대상이 채널의 음성 프리셋 목록과 일치하지 않습니다."),

    ;

    private HttpStatus status;
    private String message;
}
