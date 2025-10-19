package com.shrona.mommytalk.line.infrastructure.sender.dto.flex;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ButtonTypeDto(
    ContentType type,
    String style,
    String height,
    String margin,
    ActionDto action
) implements ContentDto {

}
