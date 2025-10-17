package com.shrona.mommytalk.kakao.infrastructure.sender.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * KakaoTalk FriendTalk 메시지 발송 응답 DTO (NHN Cloud 구조)
 */
public record KakaoFriendTalkResponseDto(
    Header header,
    Message message
) {

    /**
     * 응답 헤더
     */
    public record Header(
        Integer resultCode,
        String resultMessage,
        @JsonProperty("isSuccessful")
        Boolean isSuccessful
    ) {}

    /**
     * 메시지 정보
     */
    public record Message(
        String requestId,
        String senderGroupingKey,
        List<SendResult> sendResults
    ) {}

    /**
     * 수신자별 발송 결과
     */
    public record SendResult(
        Integer recipientSeq,
        String recipientNo,
        Integer resultCode,
        String resultMessage,
        String recipientGroupingKey
    ) {}

    // 기존 코드 호환성을 위한 헬퍼 메서드
    public Boolean isSuccessful() {
        return header != null ? header.isSuccessful() : null;
    }

    public String requestId() {
        return message != null ? message.requestId() : null;
    }

    public Integer successCount() {
        if (message == null || message.sendResults() == null) {
            return null;
        }
        return (int) message.sendResults().stream()
            .filter(r -> r.resultCode() == 0)
            .count();
    }

    public Integer failCount() {
        if (message == null || message.sendResults() == null) {
            return null;
        }
        return (int) message.sendResults().stream()
            .filter(r -> r.resultCode() != 0)
            .count();
    }

    public List<SendResult> recipientList() {
        return message != null ? message.sendResults() : null;
    }
}
