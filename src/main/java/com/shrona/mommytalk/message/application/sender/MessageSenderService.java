package com.shrona.mommytalk.message.application.sender;

import com.shrona.mommytalk.line.application.sender.LineMessageSender;
import com.shrona.mommytalk.message.infrastructure.repository.MessageQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MessageSenderService {

    private final MessageQueryRepository messageRepository;

    private final LineMessageSender lineMessageSender;

    /**
     * 서버가 재시작 시 미 발송의 이전 시간 까지의 메시지를 갖고 온다
     */
    public void sendMessageBeforeNow() {

    }

}
