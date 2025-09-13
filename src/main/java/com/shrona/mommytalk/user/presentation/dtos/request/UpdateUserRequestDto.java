package com.shrona.mommytalk.user.presentation.dtos.request;

public record UpdateUserRequestDto(
    String childName,
    String phoneNumber,
    Integer userLevel,
    Integer childLevel
) {

}
