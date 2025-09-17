package com.shrona.mommytalk.line.application.sender;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.line.domain.LineUser;
import java.util.List;

public interface LineMessageSender {

    /**
     * 예약된 메시지를 전달한다.
     */
    public void sendLineMessageByReservation();

    /**
     * ids를 기준으로 메시지를 전달한다.
     */
    public void sendLineMessageByReservationByMessageIds(List<Long> messageIds);

    /**
     * test user의 라인 계정으로 테스트 메시지를 전달한다.
     */
    public boolean sendTestLineMessage(Channel channel, String text);

    /**
     * 유저에서 싱글 메시지 전달
     */
    public void sendSingleMessageWithContents(Channel channel, LineUser lineUser, String text);
}
