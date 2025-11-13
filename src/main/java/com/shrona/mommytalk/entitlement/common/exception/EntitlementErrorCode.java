package com.shrona.mommytalk.entitlement.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EntitlementErrorCode {

    ENTITLEMENT_NOT_FOUND("상품을 찾을 수 없습니다"),
    USER_ENTITLEMENT_NOT_FOUND("유저 상품권을 찾을 수 없습니다"),
    USER_ENTITLEMENT_ALREADY_EXISTS("이미 해당 상품권을 보유하고 있습니다"),
    AUTO_ACTIVE_GROUP_NOT_FOUND("AUTO_ACTIVE 그룹을 찾을 수 없습니다"),
    AUTO_ENDED_GROUP_NOT_FOUND("AUTO_ENDED 그룹을 찾을 수 없습니다"),
    INVALID_END_DATE("종료일은 현재 날짜보다 이후여야 합니다"),
    USER_IN_MULTIPLE_AUTO_GROUPS("유저가 AUTO_ACTIVE와 AUTO_ENDED 그룹에 동시에 속해있습니다");

    private final String message;
}
