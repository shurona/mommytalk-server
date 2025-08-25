package com.shrona.mommytalk.line.infrastructure.sender.dto;

public record LineMessageMulticastContentRequestBody(
    String type,
    String text
) {

}
