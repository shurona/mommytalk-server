package com.shrona.mommytalk.user.infrastructure.repository;

import static com.shrona.mommytalk.user.domain.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public User findUserByLineId(String lineId) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(user.lineUser.lineId.eq(lineId));

        return query.selectFrom(user)
            .where(builder).fetchOne();

    }
}
