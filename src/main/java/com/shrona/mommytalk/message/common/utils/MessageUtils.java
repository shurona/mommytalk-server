package com.shrona.mommytalk.message.common.utils;

import static com.shrona.mommytalk.common.utils.StaticVariable.NO_DELAY;
import static com.shrona.mommytalk.message.domain.type.ReservationStatus.PREPARE;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.channel.domain.ChannelPlatform;
import com.shrona.mommytalk.line.application.sender.LineMessageSender;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.message.domain.MessageLog;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageUtils {

    private final TaskScheduler taskScheduler;

    private final LineMessageSender lineMessageSender;

    // MessageLog ID → ScheduledFuture 매핑 저장
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

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
    @Transactional
    public void registerTaskSchedule(List<MessageLog> messageLogList, LocalDateTime reserveTime) {
        // 메시지 로그가 비어 있으면 동작하지 않음.
        if (messageLogList.isEmpty()) {
            return;
        }

        ChannelPlatform platform = messageLogList.getFirst().getChannel().getChannelPlatform();

        switch (platform) {
            case ChannelPlatform.KAKAO -> {
                // 카카오: 개별 태스크를 등록하지 않고 KakaoMessageScheduler 폴링이 발송을 담당한다.
                log.info("[KAKAO] 플랫폼은 폴링 스케줄러가 발송하므로 태스크를 등록하지 않습니다.");
            }
            case ChannelPlatform.LINE -> {
                // 라인: 예약 시간 기준 비동기 실행
                long delaySeconds = calculateDelaySeconds(LocalDateTime.now(), reserveTime);

                Runnable task = () -> lineMessageSender.sendLineMessageByReservationByMessageIds(
                    messageLogList.stream().map(MessageLog::getId).toList(), List.of(PREPARE)
                );
                registerSchedule(task, delaySeconds);
                log.info("[{}]초 이후로 [LINE] 플랫폼 그룹 전송 실행이 등록되었습니다.", delaySeconds);
            }
        }
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

    private ScheduledFuture<?> registerSchedule(Runnable task, long delaySeconds) {
        return taskScheduler.schedule(task, Instant.now().plusSeconds(delaySeconds));

    }

    // 취소 메서드 추가
    public boolean cancelScheduledTask(Long messageLogId) {
        ScheduledFuture<?> future = scheduledTasks.remove(messageLogId);

        if (future != null && !future.isDone()) {
            boolean cancelled = future.cancel(false);
            log.info("MessageLog [{}] 스케줄 취소: {}", messageLogId, cancelled);
            return cancelled;
        }

        return false;
    }
}
