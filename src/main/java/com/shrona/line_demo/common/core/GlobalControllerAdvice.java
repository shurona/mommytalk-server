package com.shrona.line_demo.common.core;

import com.shrona.line_demo.common.dto.ErrorResponseDto;
import com.shrona.line_demo.line.common.exception.LineException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
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

}
