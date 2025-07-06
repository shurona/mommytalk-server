package com.shrona.line_demo.line.application.sender;

import com.shrona.line_demo.line.application.utils.MessageUtils;
import com.shrona.line_demo.line.domain.MessageLog;
import com.shrona.line_demo.line.infrastructure.MessageLogJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class ScheduledMessageService {

    // repository
    private final MessageLogJpaRepository messageRepository;

    // utils
    private final MessageUtils messageUtils;

    /**
     * 서버가 시작될 때 메소드 실행
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initScheduledMessage() {

        // 서버 시작할 때 쓰레드 확인
        logThreadSize();
        List<MessageLog> reservedMessageList = messageRepository.findAllByReservedMessage();

        // 반복문으로 등록해준다.
        reservedMessageList.stream()
            .forEach(rs -> {
                messageUtils.registerTaskSchedule(List.of(rs), rs.getReserveTime());
            });
    }

    /**
     * 현재 사용중인 쓰레드 크기를 확인한다.
     */
    private void logThreadSize() {
        log.info("[Thread Size] {}", Thread.getAllStackTraces().size());
    }

}
