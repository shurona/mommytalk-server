package com.shrona.line_demo.line.infrastructure.sender.dto.flex;

public record BubbleMessageDto(
    ContentType type,
    BoxTypeDto header,
    HeroDto hero,
    BoxTypeDto body,
    BoxTypeDto footer
) {

    public record HeroDto(
        String type,
        String url
    ) {

    }
}