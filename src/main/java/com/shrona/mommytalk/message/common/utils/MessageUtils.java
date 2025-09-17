package com.shrona.mommytalk.message.common.utils;

import static com.shrona.mommytalk.common.utils.StaticVariable.NO_DELAY;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.line.application.sender.LineMessageSender;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.message.domain.MessageLog;
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

    private final LineMessageSender lineMessageSender;

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

        // 메시지 Sender를 Runner로 처리
        Runnable task = () -> lineMessageSender.sendLineMessageByReservationByMessageIds(
            messageLogList.stream().map(MessageLog::getId).toList()
        );

        // 예약 시간 계산
        long delaySeconds = calculateDelaySeconds(LocalDateTime.now(), reserveTime);

        // task 등록
        taskScheduler.schedule(task, Instant.now().plusSeconds(delaySeconds));

        log.info("{}초 이후로 그룹 전송 실행이 등록되었습니다. ", delaySeconds);
    }

    /**
     * 단일 전송을 스케쥴로 등록하는 메소드
     */
    public void registerSingleTask(Channel channel, LineUser lineUser, String text,
        LocalDateTime reserveTime) {

        // 메시지 센더를 Runner로 처리
        Runnable task = () -> lineMessageSender.sendSingleMessageWithContents(
            channel, lineUser, text
        );

        // 예약 시간 계산
        long delaySeconds = calculateDelaySeconds(LocalDateTime.now(), reserveTime);

        // task 등록
        taskScheduler.schedule(task, Instant.now().plusSeconds(delaySeconds));

        log.info("{}초 이후로 단일 전송 실행이 등록되었습니다. ", delaySeconds);
    }

}
