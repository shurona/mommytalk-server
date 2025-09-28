package com.shrona.mommytalk.message.application;

import static com.shrona.mommytalk.message.common.exception.MessageErrorCode.MESSAGE_NOT_SCHEDULED_FOR_DATE;

import com.shrona.mommytalk.message.common.exception.MessageException;
import com.shrona.mommytalk.message.domain.MessageType;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageLogJpaRepository;
import com.shrona.mommytalk.message.infrastructure.repository.jpa.MessageTypeJpaRepository;
import java.time.LocalDate;
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


    @Transactional
    public MessageType createMessageType(String theme, String text, LocalDate localDate) {
        Optional<MessageType> mt = messageTypeRepository.findByTheme(theme);
        // TODO: Channel 파라미터 추가 필요
        // MessageType messageType = MessageType.of(title, text, localDate, channel);
        // return mt.orElseGet(() -> messageTypeRepository.save(messageType));
        throw new UnsupportedOperationException("Channel 파라미터가 필요합니다.");
    }

    @Override
    public MessageType findMessageTypeByDate(LocalDate localDate) {
        return messageTypeRepository.findByDeliveryTime(localDate)
            .orElseThrow(() -> new MessageException(MESSAGE_NOT_SCHEDULED_FOR_DATE));
    }
}
