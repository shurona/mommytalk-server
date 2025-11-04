package com.shrona.mommytalk.user.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OnBoardingStatus {

    TRUE("TRUE"),
    FALSE("FALSE");

    private final String code;

    /**
     * onboarding 되었는 지 확인한다.
     */
    public static Boolean isOnboarding(OnBoardingStatus status) {
        return OnBoardingStatus.TRUE.code.equals(status.code);
    }
}
