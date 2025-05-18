package com.shrona.line_demo.admin.presentation.dtos;

public record AdminUserCreateRequestDto(
    String loginId,
    String password,
    String lineId
) {

}
