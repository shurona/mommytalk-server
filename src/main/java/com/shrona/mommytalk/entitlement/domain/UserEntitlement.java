package com.shrona.mommytalk.entitlement.domain;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.entity.BaseEntity;
import com.shrona.mommytalk.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

/**
 * 유저별 상품권 관리 엔티티
 * User와 Entitlement의 매핑 테이블
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction(BaseEntity.DEFAULT_CONDITION)
@Table(
    name = "user_entitlement",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_entitlement_channel_entitlement_user",
            columnNames = {"channel_id", "entitlement_id", "user_id"}
        )
    }
)
public class UserEntitlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entitlement_id", nullable = false)
    private Entitlement entitlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntitlementStatus status;

    /**
     * 유저 상품권 생성
     */
    public static UserEntitlement createUserEntitlement(
        User user,
        Entitlement entitlement,
        Channel channel,
        LocalDate startDate,
        LocalDate endDate
    ) {
        UserEntitlement userEntitlement = new UserEntitlement();
        userEntitlement.user = user;
        userEntitlement.entitlement = entitlement;
        userEntitlement.channel = channel;
        userEntitlement.startDate = startDate;
        userEntitlement.endDate = endDate;
        userEntitlement.status = EntitlementStatus.ACTIVE;

        return userEntitlement;
    }

    /**
     * 상품권 활성화
     */
    public void activate() {
        this.status = EntitlementStatus.ACTIVE;
    }

    /**
     * 상품권 비활성화
     */
    public void deactivate() {
        this.status = EntitlementStatus.INACTIVE;
    }

    /**
     * 상품권 만료 처리
     */
    public void expire() {
        this.status = EntitlementStatus.EXPIRED;
    }

    /**
     * 현재 날짜 기준 상품권 유효성 확인
     */
    public boolean isValid(LocalDate today) {
        return status == EntitlementStatus.ACTIVE
            && !today.isBefore(startDate)
            && !today.isAfter(endDate);
    }

    /**
     * 만료 여부 확인
     */
    public boolean isExpired(LocalDate today) {
        return today.isAfter(endDate);
    }

    /**
     * 종료일 연장 (미래 날짜만 가능)
     */
    public void updateEndDate(LocalDate newEndDate) {
        if (newEndDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("종료일은 과거 날짜로 설정할 수 없습니다.");
        }
        this.endDate = newEndDate;
    }

    /**
     * 상품권 재활성화 (만료/비활성 → 활성, 날짜 재설정)
     */
    public void reactivate(LocalDate newStartDate, LocalDate newEndDate) {
        this.status = EntitlementStatus.ACTIVE;
        this.startDate = newStartDate;
        this.endDate = newEndDate;
    }
}
