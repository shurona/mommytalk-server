package com.shrona.mommytalk.entitlement.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 유저 상품권 상태
 */
@Getter
@RequiredArgsConstructor
public enum EntitlementStatus {

    ACTIVE("활성"),
    INACTIVE("비활성"),
    EXPIRED("만료");

    private final String description;
}
