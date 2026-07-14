package com.shrona.mommytalk.kakao.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class KakaoSendTimeUtilsTest {

    @Test
    public void 미래_발송일이면_선호_시간_그대로_예약_테스트() {
        // given: 내일(KST) 발송분 MessageLog (reserveTime은 서버 타임존 기준)
        LocalDateTime tomorrowReserve = LocalDateTime.now().plusDays(1);
        MessageLog messageLog = MessageLog.messageLog(null, null, tomorrowReserve, "test");

        User user = User.createUser(new PhoneNumber("010-1234-5678"));
        user.updatePreferredSendTime(LocalTime.of(15, 30));

        // when
        LocalDateTime requestDate = KakaoSendTimeUtils
            .calculateUserRequestDateKst(messageLog, user);

        // then: 발송일(KST 변환 날짜)의 15:30
        LocalDateTime expectedDate = tomorrowReserve
            .atZone(ZoneId.systemDefault())
            .withZoneSameInstant(KakaoSendTimeUtils.ZONE_KST)
            .toLocalDate()
            .atTime(LocalTime.of(15, 30));
        assertThat(requestDate).isEqualTo(expectedDate);
    }

    @Test
    public void 선호_시간이_이미_지났으면_즉시_발송_테스트() {
        // given: 어제 발송분 MessageLog (모든 선호 시간이 지난 상태)
        LocalDateTime yesterdayReserve = LocalDateTime.now().minusDays(1);
        MessageLog messageLog = MessageLog.messageLog(null, null, yesterdayReserve, "test");

        User user = User.createUser(new PhoneNumber("010-1234-5678"));

        // when
        LocalDateTime requestDate = KakaoSendTimeUtils
            .calculateUserRequestDateKst(messageLog, user);

        // then: 현재(KST) 기준 약 1분 뒤
        LocalDateTime nowKst = LocalDateTime.now(KakaoSendTimeUtils.ZONE_KST);
        assertThat(requestDate).isAfter(nowKst);
        assertThat(requestDate).isBefore(nowKst.plusMinutes(2));
    }
}
