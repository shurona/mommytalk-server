package com.shrona.mommytalk.line.application.sender;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import java.util.List;

public interface LineMessageSender {

    /**
     * ids를 기준으로 메시지를 전달한다.
     */
    void sendLineMessageByReservationByMessageIds(
        List<Long> messageIds, List<ReservationStatus> statusList);

    /**
     * test user의 라인 계정으로 테스트 메시지를 전달한다.
     */
    boolean sendTestLineMessage(Channel channel, Long messageContentId);

    /**
     * 유저에서 싱글 메시지 전달
     */
    void sendSingleMessageWithContents(Channel channel, LineUser lineUser, String text);
}
