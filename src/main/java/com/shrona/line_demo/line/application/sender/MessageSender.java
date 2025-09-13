package com.shrona.line_demo.line.application.sender;

import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.LineUser;
import java.util.List;

public interface MessageSender {

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
    public boolean sendTestLineMessage(
        Channel channel, String text, String headerLink, String bottomLink);

    /**
     * 유저에서 싱글 메시지 전달 (현재 휴대전화 등록 시 성공 메시지에 사용 중)
     */
    public void sendSingleMessageWithContents(Channel channel, LineUser lineUser, String text);
}
