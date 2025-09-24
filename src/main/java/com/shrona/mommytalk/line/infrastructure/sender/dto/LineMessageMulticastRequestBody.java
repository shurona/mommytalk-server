package com.shrona.mommytalk.line.infrastructure.sender.dto;

import com.shrona.mommytalk.line.infrastructure.sender.dto.flex.LineFlexMessageRequestDto;
import java.util.List;

public record LineMessageMulticastRequestBody(
    List<String> to,
    List<Object> messages
) {

    public static LineMessageMulticastRequestBody of(List<String> lineIdList, String content) {
        return new LineMessageMulticastRequestBody(
            lineIdList,
            List.of(new LineMessageMulticastContentRequestBody("text", content))
        );
    }

    public static LineMessageMulticastRequestBody ofFlex(List<String> lineIdList,
        LineFlexMessageRequestDto flexMessage) {
        return new LineMessageMulticastRequestBody(
            lineIdList,
            List.of(flexMessage)
        );
    }

}