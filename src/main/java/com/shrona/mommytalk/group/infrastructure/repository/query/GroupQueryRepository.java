package com.shrona.mommytalk.group.infrastructure.repository.query;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.entitlement.domain.Entitlement;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.domain.GroupType;
import com.shrona.mommytalk.group.infrastructure.dao.UserMemberCountByGroupIdsVo;
import com.shrona.mommytalk.user.domain.User;
import java.util.List;
import java.util.Optional;

public interface GroupQueryRepository {

    /**
     * 그룹에 속한 유저 목록을 갖고 온다.
     */
    List<User> findUserListByGroupIds(List<Long> groupIds);

    /**
     * 상품 정보가 담겨 있는 그룹 목록을 갖고 온다.
     */
    List<Group> findEntitlementGroupListByChannel(Long channelId);

    /**
     * 포함 및 제외에 해당하는 유저 숫자를 갖고 온다.
     */
    UserMemberCountByGroupIdsVo findUserCountInGroupAndExGroup(
        List<Long> includeGroupIds, List<Long> excludeGroupIds
    );

    /**
     * 채널, 상품, 그룹타입으로 그룹 조회
     */
    Optional<Group> findByChannelAndEntitlementAndGroupType(
        Channel channel,
        Entitlement entitlement,
        GroupType groupType
    );
}
