package com.shrona.mommytalk.group.infrastructure.repository.query;

import static com.shrona.mommytalk.group.domain.QGroup.group;
import static com.shrona.mommytalk.group.domain.QUserGroup.userGroup;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.entitlement.domain.Entitlement;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.domain.GroupType;
import com.shrona.mommytalk.group.infrastructure.dao.UserMemberCountByGroupIdsVo;
import com.shrona.mommytalk.user.domain.User;
import java.util.List;
import java.util.Optional;
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

    @Override
    public UserMemberCountByGroupIdsVo findUserCountInGroupAndExGroup(
        List<Long> includeGroupIds, List<Long> excludeGroupIds) {

        // 1. includeGroupIds에 속한 유저 ID 조회 (DISTINCT)
        List<Long> includedUserIds = query
            .select(userGroup.user.id)
            .distinct()
            .from(userGroup)
            .where(userGroup.group.id.in(includeGroupIds))
            .fetch();

        int includedCount = includedUserIds.size();

        // 2. excludeGroupIds가 비어있으면 제외할 유저 없음
        int excludedCount = 0;
        if (excludeGroupIds != null && !excludeGroupIds.isEmpty()) {
            // includeGroupIds에 속하면서 동시에 excludeGroupIds에도 속한 유저 수 (중복)
            excludedCount = query
                .select(userGroup.user.id)
                .distinct()
                .from(userGroup)
                .where(
                    userGroup.user.id.in(includedUserIds),  // 포함 그룹에 속한 유저 중에서
                    userGroup.group.id.in(excludeGroupIds)  // 제외 그룹에도 속한 유저
                )
                .fetch()
                .size();
        }

        // 3. 최종 수신자 = 포함 - 제외
        int totalRecipients = includedCount - excludedCount;

        return new UserMemberCountByGroupIdsVo(
            totalRecipients,
            includedCount,
            excludedCount
        );
    }

    @Override
    public Optional<Group> findByChannelAndEntitlementAndGroupType(
        Channel channel,
        Entitlement entitlement,
        GroupType groupType
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(group.channel.eq(channel));
        builder.and(group.entitlement.eq(entitlement));
        builder.and(group.groupType.eq(groupType));

        return Optional.ofNullable(
            query.selectFrom(group)
                .where(builder)
                .fetchOne()
        );
    }
}
