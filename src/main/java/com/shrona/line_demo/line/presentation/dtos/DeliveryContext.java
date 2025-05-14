package com.shrona.line_demo.line.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeliveryContext(
    @JsonProperty("isRedelivery") boolean isRedelivery
) {

}
