package com.shrona.mommytalk.kakao.application.sender;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import java.time.LocalTime;
import java.util.List;

public interface KakaoMessageSender {

    /**
     * MessageLog 기반 예약 메시지 전송
     *
     * @param sendTimeBeforeKst 선호 발송 시간(KST)이 이 시간 이전인 유저만 대상 (null이면 전체)
     * @return 전송 대상으로 조회되어 접수 시도한 상세(MessageLogDetail) 수
     */
    int sendKakaoMessageByReservationByMessageIds(
        List<Long> messageIds, List<ReservationStatus> statusList, LocalTime sendTimeBeforeKst);

    /**
     * 테스트 메시지 전송
     */
    boolean sendTestMessage(Channel channel, Long messageContentId);
}
