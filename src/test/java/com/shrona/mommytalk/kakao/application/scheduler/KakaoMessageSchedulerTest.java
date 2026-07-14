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
    public void 자정을_넘는_실행은_오늘_끝으로_클램프_테스트() {
        // given: 23:55 실행 → 윈도우 끝이 다음 날 00:25로 되감김
        LocalDateTime nowKst = LocalDateTime.of(2026, 7, 12, 23, 55);

        // when
        List<SubmitWindow> windows = KakaoMessageScheduler.calculateWindows(nowKst);

        // then: (오늘, 끝까지) 하나만 — 내일 선접수는 하지 않음
        // (설정 범위 07:00~20:00상 자정 구간 유저 없음 + 00:05 승격 전 선접수는 옛값 접수)
        assertThat(windows).hasSize(1);
        assertThat(windows.get(0).sendDate()).isEqualTo(LocalDate.of(2026, 7, 12));
        assertThat(windows.get(0).windowEnd()).isEqualTo(LocalTime.MAX);
    }
}
