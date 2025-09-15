package com.shrona.mommytalk.line.infrastructure.sender.dto;

import java.util.List;

public record LineMessageMulticaseTestRequestBody(
    List<String> to,
    List<Object> messages
) {

}
