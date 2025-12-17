package com.shrona.mommytalk.kakao.application.sender;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import java.util.List;

public interface KakaoMessageSender {

    /**
     * MessageLog 기반 예약 메시지 전송 (LINE과 동일한 구조)
     */
    void sendKakaoMessageByReservationByMessageIds(
        List<Long> messageIds, List<ReservationStatus> statusList);

    /**
     * 테스트 메시지 전송
     */
    boolean sendTestMessage(Channel channel, Long messageContentId);
}
