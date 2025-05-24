package com.shrona.line_demo.user.presentation.form;

import java.util.List;

public record GroupAddUserRequestBody(
    List<String> phoneNumberList
) {

}
