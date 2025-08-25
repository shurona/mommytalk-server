package com.shrona.mommytalk.user.presentation.form;

import java.util.List;

public record GroupDeleteUserRequestBody(
    List<Long> userGroupIds
) {

}
