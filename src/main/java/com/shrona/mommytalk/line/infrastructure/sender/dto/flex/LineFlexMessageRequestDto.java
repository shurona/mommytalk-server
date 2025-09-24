package com.shrona.mommytalk.line.infrastructure.sender.dto.flex;

public record LineFlexMessageRequestDto(
    ContentType type,
    String altText,
    Object contents
) {

}
