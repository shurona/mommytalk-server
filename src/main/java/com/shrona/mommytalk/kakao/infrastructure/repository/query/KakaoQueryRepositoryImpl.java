package com.shrona.mommytalk.kakao.infrastructure.repository.query;

import static com.shrona.mommytalk.kakao.domain.QChannelKakaoUser.channelKakaoUser;
import static com.shrona.mommytalk.kakao.domain.QKakaoUser.kakaoUser;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.kakao.domain.ChannelKakaoUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class KakaoQueryRepositoryImpl implements KakaoQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public List<ChannelKakaoUser> findChannelKakaoUserByUserList(List<Long> userIds) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(channelKakaoUser.kakaoUser.user.id.in(userIds));

        return query.select(channelKakaoUser)
            .from(channelKakaoUser)
            .leftJoin(channelKakaoUser.kakaoUser, kakaoUser).fetchJoin()
            .where(builder)
            .fetch();
    }
}
