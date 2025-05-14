package com.shrona.line_demo.line.presentation.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;

public record LineSource(
    @JsonProperty("type") String type,
    @JsonProperty("userId") String userId
) {

}
