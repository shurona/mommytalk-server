package com.shrona.mommytalk.common.utils;

import java.time.LocalDate;
import java.time.ZoneId;

public class DateTimeUtils {

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private DateTimeUtils() {}

    /**
     * 한국 표준시(KST) 기준의 오늘 날짜 반환.
     * 서버 JVM은 UTC로 동작하므로 비즈니스 날짜 비교 시 반드시 이 메서드를 사용해야 함.
     */
    public static LocalDate todayKst() {
        return LocalDate.now(KST);
    }
}
