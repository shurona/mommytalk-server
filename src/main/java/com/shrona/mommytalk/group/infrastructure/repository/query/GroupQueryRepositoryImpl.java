package com.shrona.mommytalk.group.infrastructure.repository.query;

import static com.shrona.mommytalk.group.domain.QGroup.group;
import static com.shrona.mommytalk.group.domain.QUserGroup.userGroup;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.domain.GroupType;
import com.shrona.mommytalk.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class GroupQueryRepositoryImpl implements GroupQueryRepository {

    private final JPAQueryFactory query;

    @Override
    public List<User> findUserListByGroupIds(List<Long> groupIds) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(userGroup.group.id.in(groupIds));

        return query.select(userGroup.user).distinct()
            .from(userGroup)
            .where(builder)
            .fetch();
    }

    @Override
    public List<Group> findEntitlementGroupListByChannel(Long channelId) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(group.channel.id.eq(channelId));

        // 상품쪽 그룹 정보를 갖고 온다.
        builder.and(group.groupType.in(GroupType.AUTO_ACTIVE, GroupType.AUTO_ENDED));

        return query.select(group)
            .from(group)
            .where(builder)
            .fetch();
    }
}
