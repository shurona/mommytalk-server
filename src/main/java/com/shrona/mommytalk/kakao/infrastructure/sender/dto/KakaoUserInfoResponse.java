package com.shrona.mommytalk.kakao.infrastructure.sender.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 사용자 정보 응답 (/v2/user/me)
 */
public record KakaoUserInfoResponse(
    @JsonProperty("id")
    Long id,  // 카카오 회원번호

    @JsonProperty("kakao_account")
    KakaoAccount kakaoAccount
) {

    /**
     * 카카오 계정 정보
     */
    public record KakaoAccount(
        @JsonProperty("profile")
        Profile profile,

        @JsonProperty("email")
        String email,

        @JsonProperty("phone_number")
        String phoneNumber  // +82 10-1234-5678 형식
    ) {

        /**
         * 프로필 정보
         */
        public record Profile(
            @JsonProperty("nickname")
            String nickname,

            @JsonProperty("profile_image_url")
            String profileImageUrl
        ) {
        }
    }

    /**
     * 카카오 ID를 문자열로 반환
     */
    public String getKakaoId() {
        return String.valueOf(id);
    }

    /**
     * 닉네임 반환
     */
    public String getNickname() {
        return kakaoAccount != null && kakaoAccount.profile != null
            ? kakaoAccount.profile.nickname
            : null;
    }

    /**
     * 프로필 이미지 URL 반환
     */
    public String getProfileImageUrl() {
        return kakaoAccount != null && kakaoAccount.profile != null
            ? kakaoAccount.profile.profileImageUrl
            : null;
    }

    /**
     * 이메일 반환
     */
    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.email : null;
    }

    /**
     * 전화번호 반환
     */
    public String getPhoneNumber() {
        return kakaoAccount != null ? kakaoAccount.phoneNumber : null;
    }
}
