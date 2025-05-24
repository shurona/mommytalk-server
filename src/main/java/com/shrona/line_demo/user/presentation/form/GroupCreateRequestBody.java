package com.shrona.line_demo.user.presentation.form;

import java.util.List;

public record GroupCreateRequestBody(
    String name,
    String description,
    List<String> phoneNumberList
) {

}
