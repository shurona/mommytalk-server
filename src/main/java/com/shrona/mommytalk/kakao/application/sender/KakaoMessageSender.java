package com.shrona.mommytalk.kakao.application.sender;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import com.shrona.mommytalk.user.domain.User;
import java.time.LocalDateTime;
import java.util.List;

public interface KakaoMessageSender {

    /**
     * MessageLog 기반 예약 메시지 전송 (LINE과 동일한 구조)
     */
    void sendKakaoMessageByReservationByMessageIds(
        List<Long> messageIds, List<ReservationStatus> statusList);

    /**
     * 단일 사용자에게 메시지 전송
     */
    void sendSingleMessage(Channel channel, User user, String content);

    /**
     * 여러 사용자에게 메시지 전송
     */
    void sendMultiMessage(Channel channel, List<User> users, String content);

    /**
     * 예약 메시지 전송 (API 레벨 예약)
     */
    void sendScheduledMessage(
        Channel channel,
        List<User> users,
        String content,
        LocalDateTime scheduledTime
    );

    /**
     * 테스트 메시지 전송
     */
    boolean sendTestMessage(Channel channel, Long messageContentId);
}
