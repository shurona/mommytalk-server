package com.shrona.mommytalk.auth.presentation.dtos.request;

public record LineAuthRequestDto(
    String code,
    String state,
    String redirectUri
) {

}
