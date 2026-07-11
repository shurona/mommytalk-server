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
    public void 야간_시간도_설정_가능_테스트() {
        // given: 알림톡은 야간 전송 제한이 없어 24시간 허용
        User user = User.createUser(new PhoneNumber("010-1234-5678"));

        // when
        user.updatePreferredSendTime(LocalTime.of(23, 0));
        // then
        assertThat(user.getPreferredSendTime()).isEqualTo(LocalTime.of(23, 0));

        // when
        user.updatePreferredSendTime(LocalTime.of(3, 30));
        // then
        assertThat(user.getPreferredSendTime()).isEqualTo(LocalTime.of(3, 30));
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
