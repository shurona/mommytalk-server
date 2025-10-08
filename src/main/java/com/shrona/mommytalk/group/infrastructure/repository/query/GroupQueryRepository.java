package com.shrona.mommytalk.group.infrastructure.repository.query;

import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.user.domain.User;
import java.util.List;

public interface GroupQueryRepository {

    /**
     * 그룹에 속한 유저 목록을 갖고 온다.
     */
    List<User> findUserListByGroupIds(List<Long> groupIds);

    /**
     * 상품 정보가 담겨 있는 그룹 목록을 갖고 온다.
     */
    List<Group> findEntitlementGroupListByChannel(Long channelId);

}
