package com.shrona.mommytalk.line.infrastructure.sender.dto.flex;

public record ActionDto(
    String type,
    String label,
    String uri
) {

}
