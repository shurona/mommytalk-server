package com.shrona.mommytalk.openai.infrastructure.repository.query;

import static com.shrona.mommytalk.openai.domain.QUserSentenceHistory.userSentenceHistory;
import static com.shrona.mommytalk.openai.domain.QUserSentenceHistoryUsageLog.userSentenceHistoryUsageLog;
import static com.shrona.mommytalk.user.domain.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.openai.domain.UserSentenceHistory;
import com.shrona.mommytalk.user.presentation.dtos.response.SentenceHistoryResponseDto;
import com.shrona.mommytalk.user.presentation.dtos.response.TokenUsageResponseDto;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public List<UserSentenceHistory> findSentenceListByUser(Long userId, Integer year,
        Integer month) {
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

    @Override
    public int countTodaySentences(Long userId, Channel channel) {
        // KST 기준 오늘 날짜 계산
        ZoneId kstZone = ZoneId.of("Asia/Seoul");
        LocalDate todayKst = LocalDate.now(kstZone);

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userSentenceHistory.user.id.eq(userId));
        builder.and(userSentenceHistory.generateDate.eq(todayKst));
        builder.and(userSentenceHistory.channel.eq(channel));

        Long count = query.select(userSentenceHistory.count())
            .from(userSentenceHistory)
            .where(builder)
            .fetchOne();

        return count != null ? count.intValue() : 0;
    }

    @Override
    public Page<SentenceHistoryResponseDto> findSentenceHistoryByChannel(
        Long channelId,
        Integer year,
        Integer month,
        Integer day,
        Long userIdFilter,
        Pageable pageable
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        // 채널 필터 (필수)
        builder.and(userSentenceHistory.channel.id.eq(channelId));

        // 유저 필터 (선택)
        if (userIdFilter != null) {
            builder.and(user.id.eq(userIdFilter));
        }

        // 날짜 필터 (선택)
        if (year != null && month != null && day != null) {
            // 특정 날짜 조회
            LocalDate targetDate = LocalDate.of(year, month, day);
            builder.and(userSentenceHistory.generateDate.eq(targetDate));
        } else if (year != null && month != null) {
            // 월 단위 조회
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);
            builder.and(userSentenceHistory.generateDate.between(startDate, endDate));
        } else if (year != null) {
            // 연 단위 조회
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            builder.and(userSentenceHistory.generateDate.between(startDate, endDate));
        }

        // 메인 쿼리 (Projection)
        JPAQuery<SentenceHistoryResponseDto> queryBuilder = query
            .select(Projections.constructor(
                SentenceHistoryResponseDto.class,
                user.id,
                userSentenceHistory.id,
                userSentenceHistory.generateDate,
                userSentenceHistory.sentence,
                userSentenceHistory.output,
                user.userLevel,
                user.childLevel,
                userSentenceHistoryUsageLog.completionTokens,
                userSentenceHistoryUsageLog.totalTokens
            ))
            .from(userSentenceHistory)
            .join(userSentenceHistory.user, user)
            .leftJoin(userSentenceHistoryUsageLog)
            .on(userSentenceHistoryUsageLog.userSentenceHistory.id.eq(userSentenceHistory.id))
            .where(builder)
            .orderBy(userSentenceHistory.generateDate.desc());

        // 페이징 적용
        List<SentenceHistoryResponseDto> content = queryBuilder
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 전체 개수 조회
        Long total = query
            .select(userSentenceHistory.count())
            .from(userSentenceHistory)
            .join(userSentenceHistory.user, user)
            .where(builder)
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    @Override
    public TokenUsageResponseDto getTokenUsageSumByChannel(
        Long channelId,
        Integer year,
        Integer month,
        Integer day,
        Long userIdFilter
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        // 채널 필터 (필수)
        builder.and(userSentenceHistory.channel.id.eq(channelId));

        // 유저 필터 (선택)
        if (userIdFilter != null) {
            builder.and(userSentenceHistory.user.id.eq(userIdFilter));
        }

        // 날짜 필터 (선택)
        if (year != null && month != null && day != null) {
            // 특정 날짜 조회
            LocalDate targetDate = LocalDate.of(year, month, day);
            builder.and(userSentenceHistory.generateDate.eq(targetDate));
        } else if (year != null && month != null) {
            // 월 단위 조회
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);
            builder.and(userSentenceHistory.generateDate.between(startDate, endDate));
        } else if (year != null) {
            // 연 단위 조회
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            builder.and(userSentenceHistory.generateDate.between(startDate, endDate));
        }

        // 토큰 합계 조회
        Integer totalCompletionTokens = query
            .select(userSentenceHistoryUsageLog.completionTokens.sum())
            .from(userSentenceHistory)
            .join(userSentenceHistory.user, user)
            .leftJoin(userSentenceHistoryUsageLog)
            .on(userSentenceHistoryUsageLog.userSentenceHistory.id.eq(userSentenceHistory.id))
            .where(builder)
            .fetchOne();

        Integer totalTokensSum = query
            .select(userSentenceHistoryUsageLog.totalTokens.sum())
            .from(userSentenceHistory)
            .join(userSentenceHistory.user, user)
            .leftJoin(userSentenceHistoryUsageLog)
            .on(userSentenceHistoryUsageLog.userSentenceHistory.id.eq(userSentenceHistory.id))
            .where(builder)
            .fetchOne();

        return TokenUsageResponseDto.of(
            totalCompletionTokens != null ? totalCompletionTokens.longValue() : null,
            totalTokensSum != null ? totalTokensSum.longValue() : null
        );
    }
}
