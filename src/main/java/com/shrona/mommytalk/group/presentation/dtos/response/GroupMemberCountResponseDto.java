package com.shrona.mommytalk.group.presentation.dtos.response;

public record GroupMemberCountResponseDto(
    Integer totalRecipients,
    Integer includedCount,
    Integer excludedCount
) {

    public static GroupMemberCountResponseDto of(
        Integer totalRecipients, Integer includedCount, Integer excludedCount) {
        return new GroupMemberCountResponseDto(totalRecipients, includedCount, excludedCount);
    }

}
