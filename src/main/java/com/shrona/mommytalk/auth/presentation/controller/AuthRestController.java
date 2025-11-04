package com.shrona.mommytalk.auth.presentation.controller;

import com.shrona.mommytalk.auth.application.LineAuthService;
import com.shrona.mommytalk.auth.presentation.dtos.request.KakaoAuthRequestDto;
import com.shrona.mommytalk.auth.presentation.dtos.request.LineAuthRequestDto;
import com.shrona.mommytalk.auth.presentation.dtos.response.UserAuthResponseDto;
import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.kakao.application.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/client/v1/")
@RestController
public class AuthRestController {

    private final LineAuthService lineAuthService;
    private final KakaoAuthService kakaoAuthService;

    @PostMapping("/line/callback")
    public ApiResponse<UserAuthResponseDto> lineLogin(
        @RequestBody LineAuthRequestDto requestDto
    ) {

        UserAuthResponseDto response = lineAuthService.processCallback(
            requestDto.code(),
            requestDto.channelCode(),
            requestDto.redirectUri()
        );

        return ApiResponse.success(response);
    }

    @PostMapping("/kakao/callback")
    public ApiResponse<UserAuthResponseDto> kakaoLogin(
        @RequestBody KakaoAuthRequestDto requestDto
    ) {
        // 카카오 사용자 정보 조회 (휴대전화 포함)
        UserAuthResponseDto userInfo = kakaoAuthService.processCallback(
            requestDto.code(),
            requestDto.channelCode(),
            requestDto.redirectUri()
        );

        return ApiResponse.success(userInfo);
    }
}
