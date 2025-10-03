package com.shrona.mommytalk.kakao.application.sender;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.user.domain.User;
import java.time.LocalDateTime;
import java.util.List;

public interface KakaoMessageSender {

    /**
     * 단일 사용자에게 메시지 전송
     */
    void sendSingleMessage(Channel channel, User user, String content);

    /**
     * 여러 사용자에게 메시지 전송
     */
    void sendMultiMessage(Channel channel, List<User> users, String content);

    /**
     * 예약 메시지 전송
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
    boolean sendTestMessage(Channel channel, String content);
}
