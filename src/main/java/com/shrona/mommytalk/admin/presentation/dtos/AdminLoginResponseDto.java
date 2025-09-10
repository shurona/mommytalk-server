package com.shrona.mommytalk.admin.presentation.dtos;

public record AdminLoginResponseDto(
    String accessToken,
    AdminUserResponseDto user
) {


    public static AdminLoginResponseDto of(String accessToken, AdminUserResponseDto responseDto) {
        return new AdminLoginResponseDto(
            accessToken,
            responseDto
        );
    }

}
