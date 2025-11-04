package com.shrona.mommytalk.kakao.infrastructure.sender;

import com.shrona.mommytalk.kakao.infrastructure.sender.dto.KakaoUserInfoResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface KakaoAuthClient {

    /**
     * Access Token으로 사용자 정보 조회 (기본 API)
     * 휴대전화 번호 포함
     */
    @GetExchange("/v2/user/me")
    KakaoUserInfoResponse getUserInfo(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );
}
