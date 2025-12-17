package com.shrona.mommytalk.kakao.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 카카오 알림톡 템플릿 코드 Enum
 * NHN Cloud 콘솔에 등록된 템플릿 코드와 매핑
 */
@Getter
@RequiredArgsConstructor
public enum KakaoAlimtalkTemplate {

    /**
     * 마미톡365 기본 템플릿
     * - 템플릿 내용: "오늘의 마미톡\n#{오늘의엄마표영어}"
     * - 버튼 1: 발음듣기 🔈 (WL) → #{발음안내링크}
     * - 버튼 2: ➕ 나만의 문장 만들기 (WL) → 고정 URL (dashboard)
     */
    MOMMYTALK365("mommytalk365", "마미톡365"),

    /**
     * 마미톡365 프리미엄 템플릿 (마미보카)
     * - 템플릿 내용: "오늘의 마미톡\n#{오늘의엄마표영어}"
     * - 버튼 1: 발음듣기 🔈 (WL) → #{발음안내링크}
     * - 버튼 2: 마미보카 💌 (WL) → #{단어안내링크}
     * - 버튼 3: ➕ 나만의 문장 만들기 (WL) → 고정 URL (dashboard)
     */
    MOMMYTALK365_PREMIUM("mommytalk365_premuim", "마미톡365 프리미엄");

    /**
     * NHN Cloud에 등록된 템플릿 코드
     */
    private final String templateCode;

    /**
     * 템플릿 설명
     */
    private final String description;
}
