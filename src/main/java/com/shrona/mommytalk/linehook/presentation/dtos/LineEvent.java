package com.shrona.mommytalk.linehook.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// 이벤트 공통 필드
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record LineEvent(
    @JsonProperty("type") String type,
    @JsonProperty("message") LineRcvMessage message,
    @JsonProperty("timestamp") Long timestamp,
    @JsonProperty("source") LineSource source,
    @JsonProperty("replyToken") String replyToken,
    @JsonProperty("mode") String mode,
    @JsonProperty("webhookEventId") String webhookEventId,
    @JsonProperty("deliveryContext") DeliveryContext deliveryContext
) {
    // 팩토리 메서드 (필요시 추가)
}