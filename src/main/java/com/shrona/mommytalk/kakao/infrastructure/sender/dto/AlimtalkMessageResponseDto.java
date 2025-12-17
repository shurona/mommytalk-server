package com.shrona.mommytalk.kakao.infrastructure.sender.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * NHN Cloud Alimtalk API 응답 DTO
 * 브랜드 메시지와 동일한 구조
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlimtalkMessageResponseDto(
    Header header,
    Message message
) {

    /**
     * 응답 헤더
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Header(
        /** 결과 코드 (0: 성공) */
        Integer resultCode,
        /** 결과 메시지 */
        String resultMessage,
        /** 성공 여부 */
        Boolean isSuccessful
    ) {

    }

    /**
     * 메시지 발송 결과
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Message(
        /** 요청 ID */
        String requestId,
        /** 발신자 그룹키 */
        String senderGroupingKey,
        /** 개별 발송 결과 목록 */
        List<SendResult> sendResults
    ) {

    }

    /**
     * 개별 발송 결과
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SendResult(
        /** 수신자 시퀀스 번호 */
        Integer recipientSeq,
        /** 수신자 번호 */
        String recipientNo,
        /** 결과 코드 (0: 성공) */
        Integer resultCode,
        /** 결과 메시지 */
        String resultMessage,
        /** 수신자 그룹키 */
        String recipientGroupingKey
    ) {

    }
}
