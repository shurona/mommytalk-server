package com.shrona.mommytalk.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    public void 선호_발송_시간_기본값은_10시_테스트() {
        // given & when
        User user = User.createUser(new PhoneNumber("010-1234-5678"));

        // then
        assertThat(user.getPreferredSendTime()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    public void 선호_발송_시간_변경_테스트() {
        // given
        User user = User.createUser(new PhoneNumber("010-1234-5678"));

        // when
        user.updatePreferredSendTime(LocalTime.of(15, 30));

        // then
        assertThat(user.getPreferredSendTime()).isEqualTo(LocalTime.of(15, 30));
    }

    @Test
    public void 대기_선호_시간_저장은_현재값을_건드리지_않는_테스트() {
        // given
        User user = User.createUser(new PhoneNumber("010-1234-5678"));

        // when
        user.updatePendingPreferredSendTime(LocalTime.of(15, 30));

        // then
        assertThat(user.getPreferredSendTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(user.getPendingPreferredSendTime()).isEqualTo(LocalTime.of(15, 30));
    }

    @Test
    public void 대기_선호_시간_재변경시_마지막_값으로_덮어쓰는_테스트() {
        // given
        User user = User.createUser(new PhoneNumber("010-1234-5678"));
        user.updatePendingPreferredSendTime(LocalTime.of(19, 30));

        // when
        user.updatePendingPreferredSendTime(LocalTime.of(8, 0));

        // then
        assertThat(user.getPendingPreferredSendTime()).isEqualTo(LocalTime.of(8, 0));
    }

    @Test
    public void 선호_발송_시간_null_입력시_무시_테스트() {
        // given
        User user = User.createUser(new PhoneNumber("010-1234-5678"));

        // when
        user.updatePreferredSendTime(null);

        // then
        assertThat(user.getPreferredSendTime()).isEqualTo(LocalTime.of(10, 0));
    }
}
