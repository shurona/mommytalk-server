package com.shrona.line_demo.line.infrastructure.sender.dto;

import java.util.List;

public record LineMessageSingleRequestBody(
    String to,
    List<LineMessageMulticastContentRequestBody> messages
) {

    public static LineMessageSingleRequestBody of(String lineId, String content) {
        return new LineMessageSingleRequestBody(
            lineId,
            List.of(new LineMessageMulticastContentRequestBody("text", content))
        );
    }

}
