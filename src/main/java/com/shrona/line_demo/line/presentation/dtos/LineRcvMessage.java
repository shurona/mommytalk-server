package com.shrona.line_demo.line.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LineRcvMessage(
    @JsonProperty("type") String type,
    @JsonProperty("id") String id,
    @JsonProperty("text") String text,
    @JsonProperty("quoteToken") String quoteToken
) {

}