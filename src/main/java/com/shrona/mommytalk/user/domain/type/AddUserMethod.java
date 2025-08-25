package com.shrona.mommytalk.user.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AddUserMethod {
    LINE("LINE"),
    PHONE_NUMBER("PHONE_NUMBER");

    private final String method;
}
