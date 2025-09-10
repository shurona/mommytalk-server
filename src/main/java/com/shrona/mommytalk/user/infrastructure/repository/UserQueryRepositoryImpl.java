package com.shrona.mommytalk.user.infrastructure.repository;

import static com.shrona.mommytalk.kakao.domain.QChannelKakaoUser.channelKakaoUser;
import static com.shrona.mommytalk.kakao.domain.QKakaoUser.kakaoUser;
import static com.shrona.mommytalk.line.domain.QChannelLineUser.channelLineUser;
import static com.shrona.mommytalk.line.domain.QLineUser.lineUser;
import static com.shrona.mommytalk.user.domain.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.infrastructure.dao.UserListProjection;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @Override
    public List<UserListProjection> findLineUsersByChannelId(Long channelId) {
        return query.select(
                Projections.constructor(UserListProjection.class,
                    user.id,
                    user.email,
                    user.name,
                    user.phoneNumber,
                    user.createdAt,
                    Expressions.constant(LocalDateTime.now()),
                    Expressions.constant(""),
                    user.lineUser.lineId,
                    user.userLevel,
                    user.childLevel,
                    user.childName,
                    channelLineUser.follow,
                    Expressions.constant(1)
                )
            )
            .from(channelLineUser)
            .join(channelLineUser.lineUser, lineUser)
            .join(lineUser.user, user)
            .where(
                channelLineUser.channel.id.eq(channelId)
                    .and(channelLineUser.follow.eq(true))
            )
            .fetch();
    }

    @Override
    public Page<UserListProjection> findLineUsersByChannelIdWithPaging(Long channelId,
        Pageable pageable) {
        // 데이터 조회
        List<UserListProjection> users = query.select(
                Projections.constructor(UserListProjection.class,
                    user.id,
                    user.email,
                    user.name,
                    user.phoneNumber,
                    user.createdAt,
                    Expressions.constant(LocalDateTime.now()),
                    Expressions.constant(""),
                    user.lineUser.lineId,
                    user.userLevel,
                    user.childLevel,
                    user.childName,
                    channelLineUser.follow,
                    Expressions.constant(1)
                )
            )
            .from(channelLineUser)
            .join(channelLineUser.lineUser, lineUser)
            .join(lineUser.user, user)
            .where(
                channelLineUser.channel.id.eq(channelId)
                    .and(channelLineUser.follow.eq(true))
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 전체 카운트 조회
        Long totalCount = query.select(channelLineUser.count())
            .from(channelLineUser)
            .where(
                channelLineUser.channel.id.eq(channelId)
                    .and(channelLineUser.follow.eq(true))
            )
            .fetchOne();

        return new PageImpl<>(users, pageable, totalCount != null ? totalCount : 0);
    }

    @Override
    public List<UserListProjection> findKakaoUsersByChannelId(Long channelId) {
        return query.select(
                Projections.constructor(UserListProjection.class,
                    user.id,
                    user.email,
                    user.name,
                    user.phoneNumber,
                    user.createdAt,
                    Expressions.constant(LocalDateTime.now()),
                    Expressions.constant(""),
                    user.kakaoUser.kakaoId,
                    user.userLevel,
                    user.childLevel,
                    user.childName,
                    channelLineUser.follow,
                    Expressions.constant(1)
                )
            )
            .from(channelKakaoUser)
            .join(channelKakaoUser.kakaoUser, kakaoUser)
            .join(kakaoUser.user, user)
            .where(
                channelKakaoUser.channel.id.eq(channelId)
                    .and(channelKakaoUser.follow.eq(true))
            )
            .fetch();
    }

    @Override
    public Page<UserListProjection> findKakaoUsersByChannelIdWithPaging(Long channelId,
        Pageable pageable) {
        // 데이터 조회
        List<UserListProjection> users = query.select(
                Projections.constructor(UserListProjection.class,
                    user.id,
                    user.email,
                    user.name,
                    user.phoneNumber,
                    user.createdAt,
                    Expressions.constant(LocalDateTime.now()),
                    Expressions.constant(""),
                    user.kakaoUser.kakaoId,
                    user.userLevel,
                    user.childLevel,
                    user.childName,
                    channelLineUser.follow,
                    Expressions.constant(1)
                )
            )
            .from(channelKakaoUser)
            .join(channelKakaoUser.kakaoUser, kakaoUser)
            .join(kakaoUser.user, user)
            .where(
                channelKakaoUser.channel.id.eq(channelId)
                    .and(channelKakaoUser.follow.eq(true))
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 전체 카운트 조회
        Long totalCount = query.select(channelKakaoUser.count())
            .from(channelKakaoUser)
            .where(
                channelKakaoUser.channel.id.eq(channelId)
                    .and(channelKakaoUser.follow.eq(true))
            )
            .fetchOne();

        return new PageImpl<>(users, pageable, totalCount != null ? totalCount : 0);
    }

    @Override
    public User findUserByUserId(Long userId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(user.id.eq(userId));

        return query.selectFrom(user)
            .leftJoin(user.lineUser, lineUser).fetchJoin()
            .leftJoin(user.kakaoUser, kakaoUser).fetchJoin()
            .where(builder)
            .fetchOne();

    }
}
