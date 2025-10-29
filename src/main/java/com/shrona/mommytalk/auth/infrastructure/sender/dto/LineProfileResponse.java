package com.shrona.mommytalk.auth.infrastructure.sender.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * LINE 프로필 조회 응답
 */
public record LineProfileResponse(
    @JsonProperty("userId")
    String userId,

    @JsonProperty("displayName")
    String displayName,

    @JsonProperty("pictureUrl")
    String pictureUrl,

    @JsonProperty("statusMessage")
    String statusMessage
) {
}
