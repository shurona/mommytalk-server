package com.shrona.line_demo.linehook.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeliveryContext(
    @JsonProperty("isRedelivery") boolean isRedelivery
) {

}
