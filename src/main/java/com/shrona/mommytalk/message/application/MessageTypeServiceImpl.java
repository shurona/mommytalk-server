package com.shrona.mommytalk.message.application;

import static com.shrona.mommytalk.channel.common.exception.ChannelErrorCode.CHANNEL_NOT_FOUND;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_NOT_SCHEDULED_FOR_DATE;
import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_TYPE_NOT_FOUND;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.message.common.exception.MessageException;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageContentJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageTypeJpaRepository;
import com.shrona.mommytalk.message.presentation.dtos.request.MessageTypeRequestDto;
import com.shrona.mommytalk.message.presentation.dtos.response.MessageTypeResponseDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Service
public class MessageTypeServiceImpl implements MessageTypeService {

    // repository
    private final MessageLogJpaRepository messageLogRepository;
    private final MessageTypeJpaRepository messageTypeRepository;
    private final MessageContentJpaRepository messageContentJpaRepository;

    // service
    private final ChannelService channelService;


    @Transactional
    public MessageType createMessageType(
        String theme, String text, LocalDate localDate, Channel channel) {
        Optional<MessageType> mt = messageTypeRepository.findByTheme(theme);
        MessageType messageType = MessageType.of(theme, text, localDate, channel);
        return mt.orElseGet(() -> messageTypeRepository.save(messageType));
    }

    @Override
    public MessageType findMessageTypeByDate(LocalDate localDate, Channel channel) {
        return messageTypeRepository.findByDeliveryTime(localDate, channel)
            .orElseThrow(() -> new MessageException(MESSAGE_NOT_SCHEDULED_FOR_DATE));
    }

    @Override
    public Optional<MessageTypeResponseDto> findMessageTypeByChannelAndDate(Long channelId,
        LocalDate date) {
        // 채널 권한 체크
        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        // MessageType 조회
        Optional<MessageType> messageType = messageTypeRepository
            .findByChannelIdAndDeliveryTime(channelId, date);

        return messageType.map(mt -> MessageTypeResponseDto.of(
            mt.getDeliveryTime(),
            mt.getTheme(),
            mt.getContext()
        ));
    }

    @Override
    @Transactional
    public MessageTypeResponseDto createMessageType(Long channelId,
        MessageTypeRequestDto requestDto) {
        // 채널 권한 체크
        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        // MessageType 생성
        MessageType messageType = MessageType.of(
            requestDto.theme(),
            requestDto.context(),
            requestDto.localDate(),
            channel
        );

        MessageType savedMessageType = messageTypeRepository.save(messageType);

        return MessageTypeResponseDto.of(
            savedMessageType.getDeliveryTime(),
            savedMessageType.getTheme(),
            savedMessageType.getContext()
        );
    }

    @Override
    @Transactional
    public MessageTypeResponseDto updateMessageType(Long channelId,
        MessageTypeRequestDto requestDto) {
        // 채널 권한 체크
        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        // 기존 MessageType 조회
        MessageType messageType = messageTypeRepository
            .findByChannelIdAndDeliveryTime(channelId, requestDto.localDate())
            .orElseThrow(() -> new MessageException(MESSAGE_TYPE_NOT_FOUND));

        // MessageType 수정
        messageType.updateContent(requestDto.theme(), requestDto.context());
        MessageType updatedMessageType = messageTypeRepository.save(messageType);

        // 해당 MessageType의 모든 MessageContent 승인 취소
        List<MessageContent> approvedContents = messageContentJpaRepository
            .findByMessageTypeAndApprovedTrue(messageType);

        for (MessageContent content : approvedContents) {
            content.cancelApproval(); // false로 변경
        }

        if (!approvedContents.isEmpty()) {
            messageContentJpaRepository.saveAll(approvedContents);
        }

        return MessageTypeResponseDto.of(
            updatedMessageType.getDeliveryTime(),
            updatedMessageType.getTheme(),
            updatedMessageType.getContext()
        );
    }
}
