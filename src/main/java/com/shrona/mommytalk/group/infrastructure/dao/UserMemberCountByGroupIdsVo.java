package com.shrona.mommytalk.group.infrastructure.dao;

public record UserMemberCountByGroupIdsVo(
    Integer totalRecipients,
    Integer includedCount,
    Integer excludedCount
) {

}
