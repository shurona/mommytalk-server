package com.shrona.mommytalk.kakao.infrastructure.sender.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * KakaoTalk FriendTalk 메시지 발송 요청 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record KakaoFriendTalkRequestDto(
    /** 발신 프로필 키 (필수) */
    String senderKey,
    /** 예약 발송 시간 (yyyy-MM-dd HH:mm), null이면 즉시 발송 */
    String requestDate,
    /** 광고성 메시지 여부 */
    Boolean isAd,
    /** 수신자 목록 (필수) */
    List<RecipientDto> recipientList,
    /** SMS/LMS 대체 발송 설정 */
    ResendParameterDto resendParameter,
    /** 통계 ID */
    String statsId
) {

    /**
     * 수신자 정보 DTO
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RecipientDto(
        /** 수신자 전화번호 (필수) */
        String recipientNo,
        /** 메시지 내용 (필수) */
        String content,
        /** 버튼 목록 (최대 5개) */
        List<ButtonDto> buttons,
        /** 이미지 URL */
        String imageUrl,
        /** 이미지 클릭 시 이동할 링크 */
        String imageLink
    ) {
    }

    /**
     * 버튼 정보 DTO
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ButtonDto(
        /** 버튼 순서 */
        String ordering,
        /** 버튼 타입 (WL: 웹링크, AL: 앱링크, BK: 봇키워드, MD: 메시지전달) */
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

    /**
     * SMS/LMS 대체 발송 설정 DTO
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ResendParameterDto(
        /** 대체 발송 여부 */
        Boolean isResend,
        /** 대체 발송 타입 (SMS, LMS) */
        String resendType,
        /** 대체 발송 발신번호 */
        String resendSenderNo,
        /** 대체 발송 내용 */
        String resendContent
    ) {
    }

    /**
     * 텍스트 메시지 생성 (단일 수신자)
     */
    public static KakaoFriendTalkRequestDto ofSingle(
        String senderKey,
        String recipientNo,
        String content
    ) {
        return new KakaoFriendTalkRequestDto(
            senderKey,
            null,
            null,
            List.of(new RecipientDto(recipientNo, content, null, null, null)),
            null,
            null
        );
    }

    /**
     * 텍스트 메시지 생성 (다중 수신자)
     */
    public static KakaoFriendTalkRequestDto ofMulti(
        String senderKey,
        List<String> recipientNos,
        String content
    ) {
        List<RecipientDto> recipients = recipientNos.stream()
            .map(recipientNo -> new RecipientDto(recipientNo, content, null, null, null))
            .toList();

        return new KakaoFriendTalkRequestDto(
            senderKey,
            null,
            null,
            recipients,
            null,
            null
        );
    }

    /**
     * 예약 발송 메시지 생성
     */
    public static KakaoFriendTalkRequestDto ofScheduled(
        String senderKey,
        List<String> recipientNos,
        String content,
        String requestDate
    ) {
        List<RecipientDto> recipients = recipientNos.stream()
            .map(recipientNo -> new RecipientDto(recipientNo, content, null, null, null))
            .toList();

        return new KakaoFriendTalkRequestDto(
            senderKey,
            requestDate,
            null,
            recipients,
            null,
            null
        );
    }

    /**
     * 버튼이 포함된 메시지 생성
     */
    public static KakaoFriendTalkRequestDto ofWithButtons(
        String senderKey,
        List<String> recipientNos,
        String content,
        List<ButtonDto> buttons
    ) {
        List<RecipientDto> recipients = recipientNos.stream()
            .map(recipientNo -> new RecipientDto(recipientNo, content, buttons, null, null))
            .toList();

        return new KakaoFriendTalkRequestDto(
            senderKey,
            null,
            null,
            recipients,
            null,
            null
        );
    }

    /**
     * 이미지가 포함된 메시지 생성
     */
    public static KakaoFriendTalkRequestDto ofWithImage(
        String senderKey,
        List<String> recipientNos,
        String content,
        String imageUrl,
        String imageLink
    ) {
        List<RecipientDto> recipients = recipientNos.stream()
            .map(recipientNo -> new RecipientDto(recipientNo, content, null, imageUrl, imageLink))
            .toList();

        return new KakaoFriendTalkRequestDto(
            senderKey,
            null,
            null,
            recipients,
            null,
            null
        );
    }
}
