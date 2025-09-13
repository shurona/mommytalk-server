package com.shrona.line_demo.line.infrastructure.sender.dto.flex;

public record LineFlexMessageRequestDto(
    ContentType type,
    String altText,
    Object contents
) {

}
