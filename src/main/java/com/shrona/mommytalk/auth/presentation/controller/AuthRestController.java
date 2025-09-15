package com.shrona.mommytalk.auth.presentation.controller;

import com.shrona.mommytalk.auth.application.LineAuthService;
import com.shrona.mommytalk.auth.presentation.dtos.request.LineAuthRequestDto;
import com.shrona.mommytalk.auth.presentation.dtos.response.LineAuthResponseDto;
import com.shrona.mommytalk.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/")
@RestController
public class AuthRestController {

    private final LineAuthService lineAuthService;

    @PostMapping("/line/callback")
    public ApiResponse<?> yahoo(
        @RequestBody LineAuthRequestDto requestDto
    ) {

        LineAuthResponseDto response = lineAuthService.processCallback(
            requestDto.code(),
            requestDto.state(),
            requestDto.redirectUri()
        );

        log.info("LINE 계정 연결 성공: {}", requestDto);

        return ApiResponse.success(response);
    }

    /**
     * 연결된 LINE 사용자 정보 조회
     */
    @GetMapping("/profile")
    public ApiResponse<?> getProfile(HttpServletRequest request) {
//        Long adminId = getCurrentAdminId(request);
//
//        try {
//            LineProfileResponse profile = lineAuthService.getUserProfile(adminId);
//            return ApiResponse.success(profile);
//
//        } catch (LinkedAccountNotFoundException e) {
//            throw new NotFoundException("No linked LINE account found");
//
//        } catch (Exception e) {
//            log.error("LINE 프로필 조회 실패", e);
//            throw new InternalServerErrorException("Profile retrieval failed");
//        }
        return ApiResponse.success("");
    }

    /**
     * LINE 계정 연결 해제
     */
    @DeleteMapping("/unlink")
    public ApiResponse<?> unlinkAccount(HttpServletRequest request) {
//        Long adminId = getCurrentAdminId(request);
//
//        try {
//            LineUnlinkResponse response = lineAuthService.unlinkAccount(adminId);
//            log.info("LINE 계정 연결 해제: adminId={}", adminId);
//            return ApiResponse.success(response);
//
//        } catch (LinkedAccountNotFoundException e) {
//            throw new NotFoundException("No linked LINE account found");
//
//        } catch (Exception e) {
//            log.error("LINE 계정 연결 해제 실패", e);
//            throw new InternalServerErrorException("Account unlink failed");
//        }
        return ApiResponse.success("");
    }

}
