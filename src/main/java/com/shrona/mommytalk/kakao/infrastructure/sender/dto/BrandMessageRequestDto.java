package com.shrona.mommytalk.kakao.infrastructure.sender.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * NHN Cloud Brand Message API 요청 DTO
 * Endpoint: POST /brand-message/v1.0/appkeys/{appkey}/freestyle-messages
 * <p>
 * 브랜드 메시지 API 특징:
 * - content와 buttons는 최상위 레벨에 위치
 * - recipientList는 전화번호(recipientNo)만 포함
 * - 개인화 메시지 미지원 (모든 수신자가 동일 content 수신)
 * - 개인화가 필요한 경우 수신자별로 개별 API 호출 필요
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BrandMessageRequestDto(
    /* 발신 프로필 키 (필수) */
    String senderKey,
    /* 채팅방 말풍선 타입 (필수) - TEXT 고정 */
    String chatBubbleType,
    /* 메시지 내용 (필수) - 최상위 레벨 */
    String content,
    /* 버튼 목록 (최대 5개) - 최상위 레벨 */
    List<ButtonDto> buttons,
    /* 푸시 알람 전송 여부 (기본값: true) */
    Boolean pushAlarm,
    /* 예약 발송 시간 (yyyy-MM-dd HH:mm), null이면 즉시 발송 */
    String requestDate,
    /* 수신자 목록 (필수) - 전화번호만 포함 */
    List<RecipientDto> recipientList,
    /* 이미지 URL */
    String imageUrl,
    /* 이미지 클릭 시 이동할 링크 */
    String imageLink
) {

    /**
     * 단일 수신자 메시지 생성
     */
    public static BrandMessageRequestDto ofSingle(
        String senderKey,
        String recipientNo,
        String content,
        List<ButtonDto> buttons,
        String requestDate
    ) {
        return new BrandMessageRequestDto(
            senderKey,
            "TEXT",
            content,
            buttons,
            true,
            requestDate,
            List.of(new RecipientDto(recipientNo, "I")),
            null,
            null
        );
    }

    /**
     * 다중 수신자 메시지 생성 (동일한 content)
     * 주의: 개인화가 필요한 경우 이 메서드 대신 ofSingle()을 수신자별로 호출
     */
    public static BrandMessageRequestDto ofMulti(
        String senderKey,
        List<String> recipientNos,
        String content,
        List<ButtonDto> buttons,
        String requestDate
    ) {
        List<RecipientDto> recipients = recipientNos.stream()
            .map(phone -> new RecipientDto(phone, null))
            .toList();

        return new BrandMessageRequestDto(
            senderKey,
            "TEXT",
            content,
            buttons,
            true,
            requestDate,
            recipients,
            null,
            null
        );
    }

    /**
     * 수신자 정보 DTO
     * 브랜드 메시지는 recipientNo와 targeting만 포함 (content, buttons는 최상위 레벨)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RecipientDto(
        /** 수신자 전화번호 (필수) */
        String recipientNo,
        /** 메시지 대상 타입 (M: 마케팅, N: 정보성, I: 정보성(송장)) */
        String targeting
    ) {

    }

    /**
     * 버튼 정보 DTO
     * 브랜드 메시지는 BC (봇채팅), BT (봇전환), BF(비즈니스폼) 타입 추가
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ButtonDto(
        /** 버튼 타입 (WL: 웹링크, AL: 앱링크, BK: 봇키워드, MD: 메시지전달, BC: 봇채팅, BT: 봇전환, BF: 비즈니스폼) */
        String type,
        /** 버튼 이름 */
        String name,
        /** 모바일 웹 링크 */
        String linkMo,
        /** PC 웹 링크 */
        String linkPc,
        /** iOS 앱 스킴 */
        String schemeIos,
        /** Android 앱 스킴 */
        String schemeAndroid
    ) {

    }
}
