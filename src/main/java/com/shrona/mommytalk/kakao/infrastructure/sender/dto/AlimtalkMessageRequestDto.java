package com.shrona.mommytalk.kakao.infrastructure.sender.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/**
 * NHN Cloud Alimtalk API 요청 DTO
 * Endpoint: POST /alimtalk/v2.3/appkeys/{appkey}/messages
 * <p>
 * 알림톡 API 특징:
 * - templateCode 필수 (사전 등록된 템플릿 코드)
 * - templateParameter로 템플릿 변수 치환 (#{key} 형태)
 * - 수신자별로 templateParameter 개인화 가능
 * - 버튼 URL도 templateParameter로 동적 주입 가능
 * - 최대 1,000명까지 한 번에 발송 가능 (개인화해도 1번 API 호출)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlimtalkMessageRequestDto(
    /* 발신 프로필 키 (필수) */
    String senderKey,
    /* 템플릿 코드 (필수, 최대 20자) */
    String templateCode,
    /* 예약 발송 시간 (yyyy-MM-dd HH:mm), null이면 즉시 발송 */
    String requestDate,
    /* 발신자 그룹키 (최대 100자) */
    String senderGroupingKey,
    /* 수신자 목록 (필수, 최대 1,000명) */
    List<RecipientDto> recipientList
) {

    /**
     * 다중 수신자 알림톡 생성 (개인화 지원)
     * - recipientList에 수신자별 templateParameter를 포함
     * - 한 번의 API 호출로 최대 1,000명에게 개인화된 메시지 전송
     */
    public static AlimtalkMessageRequestDto of(
        String senderKey,
        String templateCode,
        List<RecipientDto> recipientList,
        String requestDate
    ) {
        return new AlimtalkMessageRequestDto(
            senderKey,
            templateCode,
            requestDate,
            null,
            recipientList
        );
    }

    /**
     * 수신자 정보 DTO
     * 알림톡은 수신자별로 templateParameter를 개인화 가능
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RecipientDto(
        /** 수신자 전화번호 (필수) */
        String recipientNo,
        /** 수신자 그룹키 (최대 100자) */
        String recipientGroupingKey,
        /** 템플릿 변수 (#{key} 치환용) */
        Map<String, String> templateParameter
    ) {
        /**
         * 수신자별 개인화 데이터 생성
         */
        public static RecipientDto of(
            String recipientNo,
            Map<String, String> templateParameter
        ) {
            return new RecipientDto(recipientNo, null, templateParameter);
        }
    }
}
