package com.shrona.mommytalk.kakao.application.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.kakao.application.scheduler.dto.SubmitWindow;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class KakaoMessageSchedulerTest {

    @Test
    public void 일반_시간대에는_윈도우_하나만_계산_테스트() {
        // given
        LocalDateTime nowKst = LocalDateTime.of(2026, 7, 12, 14, 25);

        // when
        List<SubmitWindow> windows = KakaoMessageScheduler.calculateWindows(nowKst);

        // then: 오늘, 14:55까지
        assertThat(windows).hasSize(1);
        assertThat(windows.get(0).sendDate()).isEqualTo(LocalDate.of(2026, 7, 12));
        assertThat(windows.get(0).windowEnd()).isEqualTo(LocalTime.of(14, 55));
    }

    @Test
    public void 자정을_넘는_실행은_오늘_나머지와_내일_초반_윈도우로_분리_테스트() {
        // given: 23:55 실행 → 윈도우가 다음 날 00:25까지 걸침
        LocalDateTime nowKst = LocalDateTime.of(2026, 7, 12, 23, 55);

        // when
        List<SubmitWindow> windows = KakaoMessageScheduler.calculateWindows(nowKst);

        // then: (오늘, 끝까지) + (내일, 00:25까지) — 00:00 선호 유저가 정각에 발송되게 선접수
        assertThat(windows).hasSize(2);

        assertThat(windows.get(0).sendDate()).isEqualTo(LocalDate.of(2026, 7, 12));
        assertThat(windows.get(0).windowEnd()).isEqualTo(LocalTime.MAX);

        assertThat(windows.get(1).sendDate()).isEqualTo(LocalDate.of(2026, 7, 13));
        assertThat(windows.get(1).windowEnd()).isEqualTo(LocalTime.of(0, 25));
    }

    @Test
    public void 자정_경계_연말에도_다음_해_날짜로_계산_테스트() {
        // given: 12/31 23:55 → 내일은 다음 해 1/1
        LocalDateTime nowKst = LocalDateTime.of(2026, 12, 31, 23, 55);

        // when
        List<SubmitWindow> windows = KakaoMessageScheduler.calculateWindows(nowKst);

        // then
        assertThat(windows).hasSize(2);
        assertThat(windows.get(1).sendDate()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(windows.get(1).windowEnd()).isEqualTo(LocalTime.of(0, 25));
    }
}
