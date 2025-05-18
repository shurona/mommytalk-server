package com.shrona.line_demo.line.infrastructure.sender.dto;

public record LineMessageMulticastContentRequestBody(
    String type,
    String text
) {

}
