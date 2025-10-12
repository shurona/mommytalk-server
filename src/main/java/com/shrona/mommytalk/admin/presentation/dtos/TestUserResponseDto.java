package com.shrona.mommytalk.admin.presentation.dtos;

import com.shrona.mommytalk.admin.presentation.form.TestUserServiceDto;

public record TestUserResponseDto(
    Long id,
    String phoneNumber,
    String socialId
) {

    public static TestUserResponseDto of(TestUserServiceDto dto) {
        return new TestUserResponseDto(dto.id(), dto.phoneNumber(), dto.socialId());
    }

}
