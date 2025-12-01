package com.shrona.mommytalk.entitlement.infrastructure.query;

import com.shrona.mommytalk.entitlement.domain.EntitlementStatus;
import com.shrona.mommytalk.entitlement.domain.UserEntitlement;
import java.time.LocalDate;
import java.util.List;

public interface UserEntitlementQueryRepository {

    /**
     * 유저의 모든 상품권 조회
     */
    List<UserEntitlement> findByUserId(Long userId);

    /**
     * 유저의 특정 상태 상품권 조회
     */
    List<UserEntitlement> findByUserIdAndStatus(Long userId, EntitlementStatus status);

    /**
     * 유저의 활성 상품권 조회 (날짜 및 상태 확인)
     */
    List<UserEntitlement> findActiveEntitlements(Long userId, LocalDate today);

    /**
     * 만료된 상품권 조회 (배치 처리용)
     */
    List<UserEntitlement> findExpiredEntitlements(LocalDate today);

    /**
     * 유저의 특정 상품 타입 활성 상품권 조회
     */
    List<UserEntitlement> findActiveEntitlementsByType(
        Long userId,
        Long entitlementId,
        LocalDate today
    );

    /**
     * 유저의 특정 Entitlement 타입 이용권 조회 (상태 무관)
     */
    List<UserEntitlement> findByUserIdAndEntitlementId(Long userId, Long entitlementId);

    /**
     * 특정 채널과 유저에 활성 이용권이 하나라도 있는지 확인
     */
    boolean hasActiveEntitlement(Long channelId, Long userId);

    /**
     * 휴대전화 번호와 EntitlementId로 UserEntitlement 날짜 대량 업데이트
     */
    int bulkUpdateDatesByPhoneNumberAndEntitlement(
        String phoneNumber,
        Long entitlementId,
        LocalDate startDate,
        LocalDate endDate
    );
}
