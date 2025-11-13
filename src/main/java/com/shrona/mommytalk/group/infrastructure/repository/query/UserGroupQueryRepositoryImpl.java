package com.shrona.mommytalk.group.infrastructure.repository.query;

import static com.shrona.mommytalk.group.domain.QUserGroup.userGroup;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.group.domain.UserGroup;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserGroupQueryRepositoryImpl implements UserGroupQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public List<UserGroup> findByUserId(Long userId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userGroup.user.id.eq(userId));

        return query.selectFrom(userGroup)
            .where(builder)
            .fetch();
    }

    @Override
    public List<UserGroup> findByUserIdAndGroupId(Long userId, Long groupId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userGroup.user.id.eq(userId));
        builder.and(userGroup.group.id.eq(groupId));

        return query.selectFrom(userGroup)
            .where(builder)
            .fetch();
    }

    @Override
    public List<UserGroup> findByUserIdAndChannelId(Long userId, Long channelId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userGroup.user.id.eq(userId));
        builder.and(userGroup.group.channel.id.eq(channelId));

        return query.selectFrom(userGroup)
            .where(builder)
            .fetch();
    }

    @Override
    public List<UserGroup> findEntitlementGroupsByUserIdAndChannelId(Long userId, Long channelId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userGroup.user.id.eq(userId));
        builder.and(userGroup.group.channel.id.eq(channelId));
        builder.and(userGroup.group.groupType.in(
            com.shrona.mommytalk.group.domain.GroupType.AUTO_ACTIVE,
            com.shrona.mommytalk.group.domain.GroupType.AUTO_ENDED
        ));

        return query.selectFrom(userGroup)
            .leftJoin(userGroup.group).fetchJoin()
            .where(builder)
            .fetch();
    }
}
