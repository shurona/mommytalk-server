package com.shrona.mommytalk.message.presentation.dtos.request;

public record UpsertMessageContentRequestDto(
    Long messageTypeId,
    Integer userLevel,
    Integer childLevel,
    String content
) {

}
