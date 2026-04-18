package com.shrona.mommytalk.entitlement.domain;

import static org.assertj.core.api.Assertions.assertThat;

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
