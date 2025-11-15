package com.shrona.mommytalk.kakao.infrastructure.sender.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * NHN Cloud Brand Message API 응답 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BrandMessageResponseDto(
    Header header,
    List<SendResult> sendResultList
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
     * 개별 발송 결과
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SendResult(
        /** 수신자 번호 */
        String recipientNo,
        /** 결과 코드 */
        Integer resultCode,
        /** 결과 메시지 */
        String resultMessage,
        /** 요청 ID */
        String requestId,
        /** 수신자 시퀀스 번호 */
        Integer recipientSeq,
        /** 수신자 그룹 ID */
        String recipientGroupingKey
    ) {

    }
}
