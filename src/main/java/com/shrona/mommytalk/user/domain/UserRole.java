package com.shrona.mommytalk.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    // 채팅 시스템에서 필요한 롤
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN"),
    ;

    private final String authority;

}
