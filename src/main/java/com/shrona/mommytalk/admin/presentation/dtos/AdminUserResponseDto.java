package com.shrona.mommytalk.admin.presentation.dtos;

public record AdminUserResponseDto(
    Long id,
    String username,
    String name,
    String role
) {

    public static AdminUserResponseDto of(Long id, String username, String name, String role) {
        return new AdminUserResponseDto(id, username, name, role);
    }

}
