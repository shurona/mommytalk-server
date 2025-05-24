package com.shrona.line_demo.user.presentation.form;

import java.util.List;

public record GroupDeleteUserRequestBody(
    List<Long> userGroupIds
) {

}
