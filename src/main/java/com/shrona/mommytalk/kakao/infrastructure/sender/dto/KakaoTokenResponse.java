package com.shrona.mommytalk.kakao.infrastructure.sender.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 토큰 발급 응답
 */
public record KakaoTokenResponse(
    @JsonProperty("token_type")
    String tokenType,

    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("expires_in")
    Integer expiresIn,

    @JsonProperty("refresh_token")
    String refreshToken,

    @JsonProperty("refresh_token_expires_in")
    Integer refreshTokenExpiresIn,

    @JsonProperty("scope")
    String scope
) {
}
