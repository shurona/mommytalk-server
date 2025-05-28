package com.shrona.line_demo.line.application.sender;

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
     * admin user의 라인 계정으로 테스트 메시지를 전달한다.
     */
    public boolean sendTestLineMessage(String text);
}
