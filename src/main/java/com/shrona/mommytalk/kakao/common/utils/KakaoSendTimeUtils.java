package com.shrona.mommytalk.kakao.common.utils;

import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.user.domain.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * NHN 카카오 발송 예약 시간 계산 유틸
 */
public class KakaoSendTimeUtils {

    public static final ZoneId ZONE_KST = ZoneId.of("Asia/Seoul");

    private KakaoSendTimeUtils() {
    }

    /**
     * 유저의 선호 발송 시간(KST) 기반으로 NHN 예약 시간을 계산한다.
     * 발송 날짜는 MessageLog 예약 시간(UTC)을 KST로 변환한 날짜 기준이며,
     * 이미 지난 시간이면 1분 뒤 즉시 발송한다.
     */
    public static LocalDateTime calculateUserRequestDateKst(MessageLog messageLog, User user) {
        LocalDate sendDateKst = messageLog.getReserveTime()
            .atZone(ZoneId.systemDefault()) // 서버는 UTC
            .withZoneSameInstant(ZONE_KST)
            .toLocalDate();

        LocalDateTime requestDate = sendDateKst.atTime(user.getPreferredSendTime());
        LocalDateTime nowKst = LocalDateTime.now(ZONE_KST);

        return requestDate.isAfter(nowKst) ? requestDate : nowKst.plusMinutes(1);
    }
}
