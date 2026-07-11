package com.shrona.mommytalk.kakao.application.scheduler;

import static com.shrona.mommytalk.kakao.common.utils.KakaoSendTimeUtils.ZONE_KST;
import static com.shrona.mommytalk.message.domain.type.ReservationStatus.PREPARE;

import com.shrona.mommytalk.kakao.application.sender.KakaoMessageSender;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageQueryRepository;
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
     * 매시 25분/55분(KST)에 실행 — 정각/30분 선호 유저를 5분 먼저 접수해서 정시 발송을 보장한다.
     */
    @Scheduled(cron = "0 25,55 * * * *", zone = "Asia/Seoul")
    public void submitKakaoMessagesInWindow() {
        LocalDateTime nowKst = LocalDateTime.now(ZONE_KST);

        // 오늘(KST) 발송분 MessageLog 조회 (reserveTime은 UTC 저장이므로 범위 변환)
        LocalDateTime startUtc = nowKst.toLocalDate().atStartOfDay(ZONE_KST)
            .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endUtc = startUtc.plusDays(1);

        List<MessageLog> logs = messageRepository.findKakaoLogsByReserveTimeRange(startUtc, endUtc);
        if (logs.isEmpty()) {
            return;
        }

        // 윈도우 끝 계산 (자정을 넘어가면 그날 끝까지)
        LocalTime windowEnd = nowKst.toLocalTime().plusMinutes(WINDOW_MINUTES);
        if (windowEnd.isBefore(nowKst.toLocalTime())) {
            windowEnd = LocalTime.MAX;
        }

        log.info("[카카오 발송 스케줄러] 대상 MessageLog {}건, 윈도우 종료(KST): {}", logs.size(), windowEnd);

        kakaoMessageSender.sendKakaoMessageByReservationByMessageIds(
            logs.stream().map(MessageLog::getId).toList(),
            List.of(PREPARE),
            windowEnd
        );
    }
}
