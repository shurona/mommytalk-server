package com.shrona.mommytalk.kakao.infrastructure.sender.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * KakaoTalk FriendTalk 메시지 발송 응답 DTO
 */
public record KakaoFriendTalkResponseDto(
    /** 요청 ID (발송 취소 시 사용) */
    String requestId,
    /** 상태 코드 */
    String statusCode,
    /** 상태 이름 */
    String statusName,
    /** 발송 성공 여부 */
    @JsonProperty("isSuccessful")
    Boolean isSuccessful,
    /** 발송 성공 건수 */
    Integer successCount,
    /** 발송 실패 건수 */
    Integer failCount,
    /** 결과 코드 */
    String resultCode,
    /** 결과 메시지 */
    String resultMessage,
    /** 수신자별 발송 결과 목록 */
    List<RecipientResultDto> recipientList
) {

    /**
     * 수신자별 발송 결과 DTO
     */
    public record RecipientResultDto(
        /** 수신자 전화번호 */
        String recipientNo,
        /** 발송 결과 코드 */
        String resultCode,
        /** 발송 결과 메시지 */
        String resultMessage,
        /** 수신자 순번 */
        Integer recipientSeq
    ) {
    }
}
