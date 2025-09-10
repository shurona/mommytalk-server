package com.shrona.mommytalk.group.presentation.form;

import java.util.List;

public record GroupDeleteUserRequestBody(
    List<Long> userGroupIds
) {

}
