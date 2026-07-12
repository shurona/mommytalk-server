package com.shrona.mommytalk.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 선호 발송 시간 지연 적용 스케줄러
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class PreferredSendTimeScheduler {

    private final UserService userService;

    /**
     * 매일 00:02 KST에 대기 중인 선호 발송 시간(pending)을 일괄 승격
     * - 전날 변경된 선호 시간이 오늘 발송부터 적용된다 (변경은 항상 다음날부터 규칙)
     * - 첫 접수 폴링(06:55 KST) 이전이므로 발송과 충돌 없음
     */
    @Scheduled(cron = "0 2 0 * * *", zone = "Asia/Seoul")
    public void promotePendingPreferredSendTimes() {
        log.info("=== [스케줄러 시작] 선호 발송 시간 승격 (KST 00:02) ===");

        try {
            int promoted = userService.promotePendingPreferredSendTime();
            log.info("=== [스케줄러 종료] 선호 발송 시간 승격 완료: {}건 ===", promoted);
        } catch (Exception e) {
            log.error("=== [스케줄러 실패] 선호 발송 시간 승격 중 오류 발생 ===", e);
        }
    }
}
