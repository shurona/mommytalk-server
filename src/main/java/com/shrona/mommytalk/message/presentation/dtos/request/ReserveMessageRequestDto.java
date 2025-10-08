package com.shrona.mommytalk.message.presentation.dtos.request;

import java.time.ZonedDateTime;
import java.util.List;

public record ReserveMessageRequestDto(
    String deliveryDate,
    ZonedDateTime deliveryTime,
    String messageTarget, // all or groups
    Long includeGroupId,
    List<Long> includeCustomGroup,
    List<Long> excludeGroup
) {

}
