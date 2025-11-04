package com.shrona.mommytalk.openai.infrastructure.repository.query;

import static com.shrona.mommytalk.openai.domain.QUserSentenceHistory.userSentenceHistory;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.openai.domain.UserSentenceHistory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserSentenceHistoryQueryRepositoryImpl implements UserSentenceHistoryQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public UserSentenceHistory findRecentHistory(Long userId) {

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(userSentenceHistory.user.id.eq(userId));

        return query.select(userSentenceHistory)
            .from(userSentenceHistory)
            .where(builder)
            .orderBy(userSentenceHistory.createdAt.desc())
            .limit(1)
            .fetchOne();

    }

    @Override
    public List<UserSentenceHistory> findSentenceListByUser(Long userId, Integer year, Integer month) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(userSentenceHistory.user.id.eq(userId));

        // 년월 기간으로 조회 (해당 월의 첫날부터 마지막 날까지)
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        builder.and(userSentenceHistory.generateDate.between(startDate, endDate));

        return query.select(userSentenceHistory)
            .from(userSentenceHistory)
            .where(builder)
            .orderBy(userSentenceHistory.generateDate.desc())
            .fetch();
    }
}
