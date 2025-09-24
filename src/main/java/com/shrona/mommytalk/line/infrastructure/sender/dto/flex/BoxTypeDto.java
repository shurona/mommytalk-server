package com.shrona.mommytalk.line.infrastructure.sender.dto.flex;

import java.util.List;

public record BoxTypeDto(
    ContentType type,
    String layout,
    List<ContentDto> contents
) implements ContentDto {

}
