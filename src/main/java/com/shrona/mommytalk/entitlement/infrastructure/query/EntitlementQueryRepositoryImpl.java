package com.shrona.mommytalk.entitlement.infrastructure.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.entitlement.domain.Entitlement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class EntitlementQueryRepositoryImpl implements EntitlementQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public Entitlement findEntitlementByGroupId(Long groudId) {

//        BooleanBuilder builder = new BooleanBuilder();
//        builder.and(groupEntitlement.group.id.eq(groudId));
//
//        return query.select(groupEntitlement.entitlement)
//            .from(groupEntitlement)
//            .where(builder)
//            .fetchOne();
        return null;
    }
}
