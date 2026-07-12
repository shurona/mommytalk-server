package com.shrona.mommytalk.kakao.application.scheduler;

import static com.shrona.mommytalk.kakao.common.utils.KakaoSendTimeUtils.ZONE_KST;
import static com.shrona.mommytalk.message.domain.type.ReservationStatus.PREPARE;

import com.shrona.mommytalk.kakao.application.scheduler.dto.SubmitWindow;
import com.shrona.mommytalk.kakao.application.sender.KakaoMessageSender;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageQueryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 카카오 예약 메시지 폴링 스케줄러
 * <p>
 * 유저별 선호 발송 시간(preferredSendTime)에 맞춰 메시지를 보내기 위해
 * 30분마다 다음 윈도우 안에 선호 시간이 도래하는 대상을 NHN에 예약 접수한다.
 * 실제 발송 시각은 NHN API 예약(requestDate)이 보장한다.
 * <p>
 * 조회 조건이 "윈도우 끝 이전 전체"이므로 이전 실행에서 누락된 건(서버 재시작 등)도
 * 다음 실행에서 자동으로 처리된다. (접수 완료 건은 COMPLETE 상태라 중복 접수되지 않음)
 * <p>
 * 킬 스위치: kakao.polling-scheduler.enabled=false 설정 시 비활성화
 */
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(
    name = "kakao.polling-scheduler.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class KakaoMessageScheduler {

    private static final int WINDOW_MINUTES = 30;

    private final MessageQueryRepository messageRepository;
    private final KakaoMessageSender kakaoMessageSender;

    /**
     * 실행 시점 기준 접수할 (발송일, 윈도우 끝) 목록을 계산한다.
     * 자정을 넘는 실행(23:55)은 윈도우 끝을 오늘 끝으로 클램프한다.
     * 내일 선접수는 하지 않는다: 선호 시간 설정 범위(07:00~20:00)상 자정 구간
     * 유저가 없고, 00:02 승격 배치 이전에 선접수하면 옛 선호 시간으로 접수되어
     * "변경은 다음날부터 적용" 규칙이 무너지기 때문.
     */
    static List<SubmitWindow> calculateWindows(LocalDateTime nowKst) {
        LocalTime windowEnd = nowKst.toLocalTime().plusMinutes(WINDOW_MINUTES);

        // LocalTime은 자정을 넘으면 00시로 되감기므로, 현재보다 앞서면 자정을 넘은 것
        if (windowEnd.isBefore(nowKst.toLocalTime())) {
            windowEnd = LocalTime.MAX;
        }
        return List.of(new SubmitWindow(nowKst.toLocalDate(), windowEnd));
    }

    /**
     * 매시 25분/55분(KST)에 실행 — 정각/30분 선호 유저를 5분 먼저 접수해서 정시 발송을 보장한다.
     */
    @Scheduled(cron = "0 25,55 * * * *", zone = "Asia/Seoul")
    public void submitKakaoMessagesInWindow() {
        LocalDateTime nowKst = LocalDateTime.now(ZONE_KST);

        for (SubmitWindow window : calculateWindows(nowKst)) {
            submitWindow(window.sendDate(), window.windowEnd());
        }
    }

    /**
     * 발송일(KST)의 MessageLog를 조회해 윈도우 내 대상을 NHN에 접수한다.
     */
    private void submitWindow(LocalDate sendDateKst, LocalTime windowEnd) {
        // reserveTime은 UTC 저장이므로 발송일(KST)을 UTC 범위로 변환
        LocalDateTime startUtc = sendDateKst.atStartOfDay(ZONE_KST)
            .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endUtc = startUtc.plusDays(1);

        List<MessageLog> logs = messageRepository.findKakaoLogsByReserveTimeRange(startUtc, endUtc);
        if (logs.isEmpty()) {
            return;
        }

        log.info("[카카오 발송 스케줄러] 발송일(KST): {}, 대상 MessageLog {}건, 윈도우 종료(KST): {}",
            sendDateKst, logs.size(), windowEnd);

        kakaoMessageSender.sendKakaoMessageByReservationByMessageIds(
            logs.stream().map(MessageLog::getId).toList(),
            List.of(PREPARE),
            windowEnd
        );
    }
}
