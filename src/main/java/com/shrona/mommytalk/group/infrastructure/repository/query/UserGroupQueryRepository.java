package com.shrona.mommytalk.group.infrastructure.repository.query;

import com.shrona.mommytalk.group.domain.UserGroup;
import java.util.List;

public interface UserGroupQueryRepository {

    /**
     * 유저 ID로 UserGroup 목록 조회
     */
    List<UserGroup> findByUserId(Long userId);

    /**
     * 유저 ID와 그룹 ID로 UserGroup 목록 조회
     */
    List<UserGroup> findByUserIdAndGroupId(Long userId, Long groupId);

    /**
     * 유저 ID와 채널 ID로 UserGroup 목록 조회
     */
    List<UserGroup> findByUserIdAndChannelId(Long userId, Long channelId);

    /**
     * 유저 ID와 채널 ID로 AUTO_ACTIVE 또는 AUTO_ENDED 그룹만 조회 (Group fetch join)
     */
    List<UserGroup> findEntitlementGroupsByUserIdAndChannelId(Long userId, Long channelId);
}
