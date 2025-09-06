package com.shrona.mommytalk.line.infrastructure.repository;


import static com.shrona.mommytalk.line.domain.QLineUser.lineUser;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.line.domain.LineUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class LineUserQueryRepositoryImpl implements LineUserQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public LineUser findLineUserByUserId(Long userId) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(lineUser.user.id.eq(userId));

        return query.select(lineUser)
            .from(lineUser)
            .where(builder)
            .fetchOne();
    }
}
