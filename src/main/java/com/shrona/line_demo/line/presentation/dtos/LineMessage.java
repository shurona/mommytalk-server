package com.shrona.line_demo.line.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LineMessage(
    @JsonProperty("type") String type,
    @JsonProperty("id") String id,
    @JsonProperty("text") String text
) {

}