package com.shrona.mommytalk.admin.presentation.dtos;

public record AdminUserCreateRequestDto(
    String loginId,
    String password,
    String lineId
) {

}
