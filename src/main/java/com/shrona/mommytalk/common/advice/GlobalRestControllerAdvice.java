package com.shrona.mommytalk.common.advice;


import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.entitlement.common.exception.EntitlementException;
import com.shrona.mommytalk.message.common.exception.BatchValidationException;
import com.shrona.mommytalk.message.common.exception.MessageException;
import com.shrona.mommytalk.user.common.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalRestControllerAdvice {

    @ExceptionHandler(UserException.class)
    public ResponseEntity<?> handleUserException(UserException ex) {
        return ResponseEntity.status(ex.getCode().getStatus())
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MessageException.class)
    public ResponseEntity<?> handleMessageException(MessageException ex) {
        return ResponseEntity.status(ex.getCode().getStatus())
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(EntitlementException.class)
    public ResponseEntity<?> handleEntitlementException(EntitlementException ex) {
        return ResponseEntity.status(ex.getCode().getStatus())
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(BatchValidationException.class)
    public ResponseEntity<?> handleBatchValidationException(BatchValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage(), ex.getData()));
    }

    /**
     * Runtime 500에러
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {

        // 500 runtime은 로그를 남긴다.
        log.error("e: ", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
