package com.shrona.mommytalk.entitlement.domain;

import lombok.Getter;

@Getter
public enum EntitlementType {

    MOMMYTALK("MOMMYTALK"),
    MOMMYVOCA("MOMMYVOCA");

    private final String product;

    EntitlementType(String product) {
        this.product = product;
    }
}
