package com.shrona.line_demo.line.application.sender;

public interface MessageSender {

    public void sendLineMessageByReservation();

    public boolean sendTestLineMessage(String text);
}
