package com.shrona.mommytalk.linehook.presentation.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeliveryContext(
    @JsonProperty("isRedelivery") boolean isRedelivery
) {

}
