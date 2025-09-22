package com.shrona.mommytalk.message.application;


import com.shrona.mommytalk.message.domain.MessageType;
import java.time.LocalDate;

public interface MessageTypeService {

    /**
     * 메시지 타입 생성
     */
    MessageType createMessageType(String title, String text, LocalDate localDate);

    /**
     * 해당 날짜에 메시지 타입이 존재하는 지 확인한다.
     */
    MessageType findMessageTypeByDate(LocalDate localDate);

}
