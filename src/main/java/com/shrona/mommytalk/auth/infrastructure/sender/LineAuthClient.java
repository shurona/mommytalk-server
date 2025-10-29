package com.shrona.mommytalk.auth.infrastructure.sender;

import com.shrona.mommytalk.auth.infrastructure.sender.dto.LineProfileResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface LineAuthClient {

    /**
     * Access Token으로 사용자 프로필 조회
     */
    @GetExchange("/v2/profile")
    LineProfileResponse getUserProfile(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );
}
