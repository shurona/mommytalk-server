package com.shrona.mommytalk.common.advice;


import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.user.common.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalRestControllerAdvice {

    @ExceptionHandler(UserException.class)
    public ApiResponse<?> handleUserException(UserException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    /**
     * Runtime 500에러
     */
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<Void> handleRuntimeException(RuntimeException ex) {

        // 500 runtime은 로그를 남긴다.
        log.error("e: ", ex);

        return ApiResponse.error(ex.getMessage());
    }
}
