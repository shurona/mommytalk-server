package com.shrona.mommytalk.common.advice;

import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.line.common.exception.LineException;
import com.shrona.mommytalk.user.common.exception.UserException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

//@ControllerAdvice
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
    public ApiResponse<?> handleUserException(LineException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(UserException.class)
    public ApiResponse<?> handleUserException(UserException ex) {
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleUnExpectError(RuntimeException ex) {
        // 예외 로깅 등 추가 가능
        // 에러 메시지, 코드 등 원하는 형태의 DTO로 응답
        log.error("[전역 에러 발생] {}", ex.getMessage());

        return "redirect:/error/500";
    }

    /**
     * 잘못된 405 요청 시 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public String handle405(HttpServletRequest req) {
        return "redirect:/admin";
    }

}
