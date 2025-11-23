package com.shrona.mommytalk.entitlement.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 유저 상품권 자동 처리 스케줄러
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class UserEntitlementScheduler {

    private final UserEntitlementService userEntitlementService;

    /**
     * 매일 자정(00:00 KST)에 만료된 상품권 자동 처리
     * - 만료된 상품권 상태를 EXPIRED로 변경
     * - AUTO_ACTIVE → AUTO_ENDED 그룹 이동
     */
    @Scheduled(cron = "0 0 15 * * *", zone = "UTC")
    public void processExpiredEntitlements() {
        log.info("=== [스케줄러 시작] 만료된 상품권 자동 처리 (UTC 15:00 / KST 00:00) ===");

        try {
            userEntitlementService.processExpiredEntitlements();
            log.info("=== [스케줄러 종료] 만료된 상품권 자동 처리 완료 ===");
        } catch (Exception e) {
            log.error("=== [스케줄러 실패] 만료된 상품권 처리 중 오류 발생 ===", e);
        }
    }
}
