package com.shrona.mommytalk.group.presentation.dtos.request;

import java.util.List;

public record GroupMemberCountRequestDto(
    String messageTarget,
    List<Long> includeGroupIds,
    List<Long> excludeGroupIds

) {

}
