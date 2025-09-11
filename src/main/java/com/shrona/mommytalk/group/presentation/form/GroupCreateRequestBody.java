package com.shrona.mommytalk.group.presentation.form;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record GroupCreateRequestBody(
    @NotBlank
    String name,
    @NotBlank
    String description,
    List<String> phoneNumberList
) {

}
