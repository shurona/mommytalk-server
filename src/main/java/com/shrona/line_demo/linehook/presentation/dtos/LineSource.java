package com.shrona.line_demo.linehook.presentation.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;

public record LineSource(
    @JsonProperty("type") String type,
    @JsonProperty("userId") String userId
) {

}
