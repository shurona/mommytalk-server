package com.shrona.mommytalk.message.application;


import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.presentation.dtos.request.MessageTypeRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageTypeResponseDto;
import java.time.LocalDate;
import java.util.Optional;

public interface MessageTypeService {

    /**
     * 메시지 타입 생성
     */
    MessageType createMessageType(String title, String text, LocalDate localDate, Channel channel);

    /**
     * 해당 날짜에 메시지 타입이 존재하는 지 확인한다.
     */
    MessageType findMessageTypeByDate(LocalDate localDate, Channel channel);

    /**
     * 채널별 날짜로 MessageType 조회
     */
    Optional<MessageTypeResponseDto> findMessageTypeByChannelAndDate(Long channelId,
        LocalDate date);

    /**
     * 채널별 MessageType 생성
     */
    MessageTypeResponseDto createMessageType(Long channelId, MessageTypeRequestDto requestDto);

    /**
     * 채널별 MessageType 수정 (기존 MessageContent들의 승인 상태 취소)
     */
    MessageTypeResponseDto updateMessageType(Long channelId, MessageTypeRequestDto requestDto);

}
