package com.shrona.mommytalk.group.presentation.form;

import java.util.List;

public record GroupAddUserRequestBody(
    List<String> phoneNumberList
) {

}
