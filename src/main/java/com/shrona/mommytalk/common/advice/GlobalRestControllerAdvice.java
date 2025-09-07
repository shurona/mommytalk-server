package com.shrona.mommytalk.common.advice;


import com.shrona.mommytalk.common.dto.ErrorResponseDto;
import com.shrona.mommytalk.user.common.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalRestControllerAdvice {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponseDto> handleUserException(UserException ex) {
        ErrorResponseDto response = new ErrorResponseDto(
            ex.getMessage());
        return ResponseEntity.status(ex.getCode().getStatus()).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleUnExpectError(RuntimeException ex) {
        // 예외 로깅 등 추가 가능
        // 에러 메시지, 코드 등 원하는 형태의 DTO로 응답
        log.error("[전역 에러 발생]", ex);

        ErrorResponseDto response = new ErrorResponseDto(ex.getMessage());
        return ResponseEntity.status(500).body(response);
    }


}
