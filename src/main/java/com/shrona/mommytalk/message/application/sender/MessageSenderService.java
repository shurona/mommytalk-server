package com.shrona.mommytalk.message.application.sender;

import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.infrastructure.repository.MessageQueryRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MessageSenderService {

    private final MessageQueryRepository messageRepository;

    /**
     * 이전 시간 까지의 메시지를 갖고 온다
     */
    public void sendMessageBeforeNow() {

        List<MessageLog> allByReservedMessageBeforeNow =
            messageRepository.findAllByReservedMessageBeforeDate(LocalDateTime.now());


    }

}
