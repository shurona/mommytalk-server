package com.shrona.mommytalk.entitlement.infrastructure.query;

import static com.shrona.mommytalk.entitlement.domain.QUserEntitlement.userEntitlement;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.entitlement.domain.EntitlementStatus;
import com.shrona.mommytalk.entitlement.domain.UserEntitlement;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserEntitlementQueryRepositoryImpl implements UserEntitlementQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public List<UserEntitlement> findByUserId(Long userId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userEntitlement.user.id.eq(userId));

        return query.selectFrom(userEntitlement)
            .where(builder)
            .orderBy(userEntitlement.createdAt.desc())
            .fetch();
    }

    @Override
    public List<UserEntitlement> findByUserIdAndStatus(Long userId, EntitlementStatus status) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userEntitlement.user.id.eq(userId));
        builder.and(userEntitlement.status.eq(status));

        return query.selectFrom(userEntitlement)
            .where(builder)
            .orderBy(userEntitlement.createdAt.desc())
            .fetch();
    }

    @Override
    public List<UserEntitlement> findActiveEntitlements(Long userId, LocalDate today) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userEntitlement.user.id.eq(userId));
        builder.and(userEntitlement.status.eq(EntitlementStatus.ACTIVE));
        builder.and(userEntitlement.startDate.loe(today));  // startDate <= today
        builder.and(userEntitlement.endDate.goe(today));    // endDate >= today

        return query.selectFrom(userEntitlement)
            .where(builder)
            .orderBy(userEntitlement.endDate.asc())
            .fetch();
    }

    @Override
    public List<UserEntitlement> findExpiredEntitlements(LocalDate today) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userEntitlement.status.eq(EntitlementStatus.ACTIVE));
        builder.and(userEntitlement.endDate.lt(today));  // endDate < today

        return query.selectFrom(userEntitlement)
            .where(builder)
            .fetch();
    }

    @Override
    public List<UserEntitlement> findActiveEntitlementsByType(
        Long userId,
        Long entitlementId,
        LocalDate today
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userEntitlement.user.id.eq(userId));
        builder.and(userEntitlement.entitlement.id.eq(entitlementId));
        builder.and(userEntitlement.status.eq(EntitlementStatus.ACTIVE));
        builder.and(userEntitlement.startDate.loe(today));
        builder.and(userEntitlement.endDate.goe(today));

        return query.selectFrom(userEntitlement)
            .where(builder)
            .orderBy(userEntitlement.endDate.asc())
            .fetch();
    }

    @Override
    public List<UserEntitlement> findByUserIdAndEntitlementId(Long userId, Long entitlementId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userEntitlement.user.id.eq(userId));
        builder.and(userEntitlement.entitlement.id.eq(entitlementId));

        return query.selectFrom(userEntitlement)
            .where(builder)
            .orderBy(userEntitlement.createdAt.desc())
            .fetch();
    }

    @Override
    public boolean hasActiveEntitlement(Long channelId, Long userId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userEntitlement.channel.id.eq(channelId));
        builder.and(userEntitlement.user.id.eq(userId));
        builder.and(userEntitlement.status.eq(EntitlementStatus.ACTIVE));

        Integer count = query.selectOne()
            .from(userEntitlement)
            .where(builder)
            .fetchFirst();

        return count != null;
    }
}
