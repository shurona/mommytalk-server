package com.shrona.mommytalk.kakao.presentation.controller;

import com.shrona.mommytalk.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/channels/{channelId}/kakao")
@RestController
public class KakaoRestController {

    /**
     * 카카오 신규 유저 등록
     */
    public ApiResponse<?> registerKakaoUser() {

        return ApiResponse.success(null);
    }

}
