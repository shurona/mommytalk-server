package com.shrona.mommytalk.user.infrastructure.repository;

import static com.shrona.mommytalk.channel.domain.QChannel.channel;
import static com.shrona.mommytalk.kakao.domain.QChannelKakaoUser.channelKakaoUser;
import static com.shrona.mommytalk.kakao.domain.QKakaoUser.kakaoUser;
import static com.shrona.mommytalk.line.domain.QChannelLineUser.channelLineUser;
import static com.shrona.mommytalk.line.domain.QLineUser.lineUser;
import static com.shrona.mommytalk.user.domain.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.user.domain.User;
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
    public List<User> findUserList(Long channelId) {

        // ChannelLineUser를 기점으로 시작하여 N+1 문제 방지
        return query.select(user)
            .from(channelLineUser)
            .join(channelLineUser.lineUser, lineUser)
            .join(lineUser.user, user)
            .where(
                channelLineUser.channel.id.eq(channelId)
                    .and(channelLineUser.follow.eq(true))
            )
            .fetch();
    }

    /**
     * 채널에 속한 모든 유저 (follow 상태 관계없이)
     */
    public List<User> findAllUsersByChannelId(Long channelId) {

        return query.select(user)
            .from(user)
            .join(user.lineUser, lineUser)
            .join(lineUser.channelLineUserList, channelLineUser)
            .join(channelLineUser.channel, channel)
            .where(channel.id.eq(channelId))
            .distinct()
            .fetch();
    }

    /**
     * 페이징 지원 - 채널에 속한 유저 목록 (N+1 문제 방지)
     */
    public Page<User> findUserListWithPaging(Long channelId, Pageable pageable) {

        // 데이터 조회
        List<User> users = query.select(user)
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

    /**
     * 멀티플랫폼 지원 - 채널에 속한 유저 (LINE + Kakao 통합, 중복 제거)
     * 향후 카카오 확장 시 사용
     */
    public List<User> findAllChannelUsers(Long channelId) {

        // 방법 1: EXISTS 서브쿼리로 중복 방지 (성능 우수)
        return query.selectFrom(user)
            .where(
                // LINE 사용자 조건
                query.selectOne()
                    .from(channelLineUser)
                    .join(channelLineUser.lineUser, lineUser)
                    .where(
                        lineUser.user.eq(user)
                            .and(channelLineUser.channel.id.eq(channelId))
                            .and(channelLineUser.follow.eq(true))
                    )
                    .exists()
                    // 향후 Kakao 조건 추가 시:
                    .or(
                        query.selectOne()
                            .from(channelKakaoUser)
                            .join(channelKakaoUser.kakaoUser, kakaoUser)
                            .where(
                                kakaoUser.user.eq(user)
                                    .and(channelKakaoUser.channel.id.eq(channelId))
                                    .and(channelKakaoUser.follow.eq(true))
                            )
                            .exists()
                    )
            )
            .fetch();
    }
    
    /**
     * 멀티플랫폼 지원 - 페이징 지원 버전 (LINE + Kakao 통합, 중복 제거)
     */
    public Page<User> findAllChannelUsersWithPaging(Long channelId, Pageable pageable) {
        
        // 데이터 조회 - EXISTS 서브쿼리로 중복 방지
        List<User> users = query.selectFrom(user)
            .where(
                // LINE 사용자 조건
                query.selectOne()
                    .from(channelLineUser)
                    .join(channelLineUser.lineUser, lineUser)
                    .where(
                        lineUser.user.eq(user)
                        .and(channelLineUser.channel.id.eq(channelId))
                        .and(channelLineUser.follow.eq(true))
                    )
                    .exists()
                // 향후 Kakao 조건 (현재 코드에서는 활성화됨)
                .or(
                    query.selectOne()
                        .from(channelKakaoUser)
                        .join(channelKakaoUser.kakaoUser, kakaoUser)
                        .where(
                            kakaoUser.user.eq(user)
                            .and(channelKakaoUser.channel.id.eq(channelId))
                            .and(channelKakaoUser.follow.eq(true))
                        )
                        .exists()
                )
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
        
        // 전체 카운트 조회 - 동일한 조건으로
        Long totalCount = query.select(user.count())
            .from(user)
            .where(
                // LINE 사용자 조건
                query.selectOne()
                    .from(channelLineUser)
                    .join(channelLineUser.lineUser, lineUser)
                    .where(
                        lineUser.user.eq(user)
                        .and(channelLineUser.channel.id.eq(channelId))
                        .and(channelLineUser.follow.eq(true))
                    )
                    .exists()
                // 향후 Kakao 조건
                .or(
                    query.selectOne()
                        .from(channelKakaoUser)
                        .join(channelKakaoUser.kakaoUser, kakaoUser)
                        .where(
                            kakaoUser.user.eq(user)
                            .and(channelKakaoUser.channel.id.eq(channelId))
                            .and(channelKakaoUser.follow.eq(true))
                        )
                        .exists()
                )
            )
            .fetchOne();
        
        return new PageImpl<>(users, pageable, totalCount != null ? totalCount : 0);
    }

}
