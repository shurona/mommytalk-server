package com.shrona.mommytalk.group.presentation.dtos.request;

import java.util.List;

public record AddUserGroupRequestDto(
    List<String> phoneNumbers
) {

}
