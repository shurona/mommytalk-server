package com.shrona.mommytalk.admin.presentation.controller;

import com.shrona.mommytalk.admin.application.AdminService;
import com.shrona.mommytalk.admin.presentation.dtos.AdminUserCreateRequestDto;
import com.shrona.mommytalk.line.application.LineService;
import com.shrona.mommytalk.linehook.application.ChannelHookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로컬 테스트 용 클래스
 */
@Profile({"local"})
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@RestController
public class AdminRestController {

    private final AdminService adminService;
    private final LineService lineService;
    private final ChannelHookService channelHookService;

    @Value("${admin.header-key}")
    private String headerKey;

    @Value("${admin.header-value}")
    private String headerValue;

    @PostMapping
    public ResponseEntity<?> createAdminUser(
        HttpServletRequest request,
        @RequestBody AdminUserCreateRequestDto requestDto
    ) {

        String inputAdminKey = request.getHeader(headerKey);

        if (!inputAdminKey.equals(headerValue)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        adminService.createAdminUser(requestDto.loginId(), requestDto.password(),
            requestDto.lineId());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/line-test")
    public ResponseEntity<?> lineTestController(
        HttpServletRequest request,
        @RequestHeader("line") String lineId,
        @RequestBody String content
    ) {
        String inputAdminKey = request.getHeader(headerKey);

        if (!inputAdminKey.equals(headerValue)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean isPhoneSave = channelHookService.saveLineMessage(1L, lineId, content);

        // 휴대번호가 저장되었으면 메시지 전송 로직 실행
        if (isPhoneSave) {
            channelHookService.sendLineMessageAfterSuccess(
                1L,
                lineId,
                content);
        }

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<String> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        // 예외 메시지 로그 남기기
        System.err.println("Missing header: " + ex.getHeaderName());

        // 클라이언트에 400 Bad Request와 메시지 전송
        return ResponseEntity
            .badRequest().build();
    }
}
