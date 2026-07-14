package com.shrona.mommytalk.kakao.application.scheduler.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 카카오 폴링 스케줄러의 접수 단위 — 발송일(KST)과 해당 실행에서 접수할 윈도우 끝 시간
 */
public record SubmitWindow(
    LocalDate sendDate,
    LocalTime windowEnd
) {

}
