package com.shrona.mommytalk.auth.presentation.dtos.response;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record LineAuthResponseDto(
    Long id,
    String lineId,
    String name,
    String email
) {

    public static LineAuthResponseDto of() {
        return LineAuthResponseDto
            .builder()
            .id(1L)
            .lineId("라인 아이디")
            .name("이름")
            .email("abc@abcde.abc")
            .build();
    }

}
