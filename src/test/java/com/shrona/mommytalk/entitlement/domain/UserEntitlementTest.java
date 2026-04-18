package com.shrona.mommytalk.entitlement.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shrona.mommytalk.common.utils.DateTimeUtils;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserEntitlementTest {

    private UserEntitlement createEntitlement(LocalDate endDate) {
        return UserEntitlement.createUserEntitlement(null, null, null,
            DateTimeUtils.todayKst(), endDate);
    }

    @Test
    @DisplayName("어제 날짜로 종료일 변경 시 예외 발생")
    void 어제_날짜로_종료일_변경시_예외() {
        // given
        LocalDate yesterday = DateTimeUtils.todayKst().minusDays(1);
        UserEntitlement ue = createEntitlement(DateTimeUtils.todayKst().plusDays(10));

        // when / then
        assertThatThrownBy(() -> ue.updateEndDate(yesterday))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("오늘 날짜로 종료일 변경 성공")
    void 오늘_날짜로_종료일_변경_성공() {
        // given
        LocalDate today = DateTimeUtils.todayKst();
        UserEntitlement ue = createEntitlement(today.plusDays(10));

        // when
        ue.updateEndDate(today);

        // then
        assertThat(ue.getEndDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("내일 날짜로 종료일 변경 성공")
    void 내일_날짜로_종료일_변경_성공() {
        // given
        LocalDate tomorrow = DateTimeUtils.todayKst().plusDays(1);
        UserEntitlement ue = createEntitlement(DateTimeUtils.todayKst());

        // when
        ue.updateEndDate(tomorrow);

        // then
        assertThat(ue.getEndDate()).isEqualTo(tomorrow);
    }
}
