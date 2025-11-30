package com.shrona.mommytalk.group.infrastructure.repository.query;

import static com.shrona.mommytalk.group.domain.QUserGroup.userGroup;
import static com.shrona.mommytalk.line.domain.QLineUser.lineUser;
import static com.shrona.mommytalk.user.domain.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.domain.UserGroup;
import io.micrometer.common.util.StringUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @Override
    public Page<UserGroup> findByGroupIdWithPaging(Group group, Pageable pageable, String searchToken) {
        BooleanBuilder searchCondition = buildUserGroupSearchCondition(searchToken);

        // 데이터 조회
        List<UserGroup> userGroups = query.selectFrom(userGroup)
            .leftJoin(userGroup.user, user).fetchJoin()
            .leftJoin(user.lineUser, lineUser).fetchJoin()
            .where(
                userGroup.group.eq(group)
                    .and(searchCondition)
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 전체 카운트 조회
        Long totalCount = query.select(userGroup.count())
            .from(userGroup)
            .where(
                userGroup.group.eq(group)
                    .and(searchCondition)
            )
            .fetchOne();

        return new PageImpl<>(userGroups, pageable, totalCount != null ? totalCount : 0);
    }

    /**
     * 그룹 멤버 검색 조건 (휴대전화, 이메일, 이름)
     */
    private BooleanBuilder buildUserGroupSearchCondition(String searchToken) {
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.isBlank(searchToken)) {
            return builder;
        }

        builder.and(
            userGroup.user.phoneNumber.phoneNumber.contains(searchToken)
                .or(userGroup.user.email.contains(searchToken))
                .or(userGroup.user.name.contains(searchToken))
        );

        return builder;
    }
}
