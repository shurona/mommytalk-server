package com.shrona.line_demo.line.application.utils;

import static com.shrona.line_demo.common.core.StaticVariable.NO_DELAY;

import com.shrona.line_demo.line.application.sender.MessageSender;
import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.domain.MessageLog;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageUtils {

    private final TaskScheduler taskScheduler;

    private final MessageSender messageSender;

    /**
     * 두 시간 사이의 초를 계산한다.
     */
    public long calculateDelaySeconds(LocalDateTime now, LocalDateTime targetDateTime) {
        Duration duration = Duration.between(now, targetDateTime);
        return Math.max(NO_DELAY, duration.getSeconds());
    }

    /**
     * 여러 전송을 스케쥴로 등록하는 메소드
     */
    public void registerTaskSchedule(List<MessageLog> messageLogList, LocalDateTime reserveTime) {

        // 메시지 센더를 Runner로 처리
        Runnable task = () -> messageSender.sendLineMessageByReservationByMessageIds(
            messageLogList.stream().map(MessageLog::getId).toList()
        );

        // 예약 시간 계산
        long delaySeconds = calculateDelaySeconds(LocalDateTime.now(), reserveTime);

        // task 등록
        taskScheduler.schedule(task, Instant.now().plusSeconds(delaySeconds));

        log.info("{}초 이후로 실행이 등록되었습니다. ", delaySeconds);
    }

    /**
     * 단일 전송을 스케쥴로 등록하는 메소드
     */
    public void registerSingleTask(Channel channel, LineUser lineUser, String text,
        LocalDateTime reserveTime) {

        // 메시지 센더를 Runner로 처리
        Runnable task = () -> messageSender.sendSingleMessageWithContents(
            channel, lineUser, text
        );

        // 예약 시간 계산
        long delaySeconds = calculateDelaySeconds(LocalDateTime.now(), reserveTime);

        // task 등록
        taskScheduler.schedule(task, Instant.now().plusSeconds(delaySeconds));

        log.info("{}초 이후로 실행이 등록되었습니다. ", delaySeconds);
    }

}
