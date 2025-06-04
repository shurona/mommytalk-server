package com.shrona.line_demo.common.core;

import com.shrona.line_demo.common.dto.ErrorResponseDto;
import com.shrona.line_demo.line.common.exception.LineException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

    /**
     * model로 currentPath를 기본적으로 등록한다.
     */
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ExceptionHandler(LineException.class)
    public ResponseEntity<ErrorResponseDto> handleLineException(LineException ex) {
        ErrorResponseDto response = new ErrorResponseDto(
            ex.getMessage());
        return ResponseEntity.status(ex.getCode().getStatus()).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handle(RuntimeException ex) {
        // 예외 로깅 등 추가 가능
        // 에러 메시지, 코드 등 원하는 형태의 DTO로 응답
        log.error("[전역 에러 발생] {}", ex.getMessage());

        ErrorResponseDto response = new ErrorResponseDto(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
