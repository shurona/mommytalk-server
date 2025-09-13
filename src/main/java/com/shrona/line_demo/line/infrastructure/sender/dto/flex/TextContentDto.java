package com.shrona.line_demo.line.infrastructure.sender.dto.flex;

public record TextContentDto(
    ContentType type,
    String text,
    Boolean wrap
) implements ContentDto {

}
