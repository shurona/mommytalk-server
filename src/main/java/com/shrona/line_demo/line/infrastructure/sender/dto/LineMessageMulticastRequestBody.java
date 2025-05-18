package com.shrona.line_demo.line.infrastructure.sender.dto;

import java.util.List;

public record LineMessageMulticastRequestBody(
    List<String> to,
    List<LineMessageMulticastContentRequestBody> messages
) {

    public static LineMessageMulticastRequestBody of(List<String> lineIdList, String content) {
        return new LineMessageMulticastRequestBody(
            lineIdList,
            List.of(new LineMessageMulticastContentRequestBody("text", content))
        );
    }

}
