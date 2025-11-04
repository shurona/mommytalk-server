package com.shrona.mommytalk.auth.presentation.dtos.request;

public record KakaoAuthRequestDto(
    String code,
    String state,
    String redirectUri,
    String channelCode
) {

}
