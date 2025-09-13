package com.shrona.line_demo.line.infrastructure.sender.dto.flex;

public record ActionDto(
    String type,
    String label,
    String uri
) {

}
