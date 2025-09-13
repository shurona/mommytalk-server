package com.shrona.line_demo.line.infrastructure.sender.dto.flex;

public record ButtonTypeDto(
    ContentType type,
    String style,
    String height,
    ActionDto action
) implements ContentDto {

}
