package com.shrona.line_demo.user.infrastructure;

import com.shrona.line_demo.user.domain.Group;
import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.UserGroup;
import com.shrona.line_demo.user.infrastructure.dao.GroupUserCount;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserGroupJpaRepository extends JpaRepository<UserGroup, Long> {

    /**
     * 전송을 위한 user의 count 조회
     */
    @Query("SELECT new com.shrona.line_demo.user.infrastructure.dao.GroupUserCount(gu.group.id, COUNT(gu)) FROM UserGroup gu WHERE gu.group.id IN :groupIds AND gu.user.lineId is not null GROUP BY gu.group.id")
    List<GroupUserCount> countByGroupIds(List<Long> groupIds);

    /**
     * 전송을 위한 user의 count 조회
     */
    @Query("SELECT new com.shrona.line_demo.user.infrastructure.dao.GroupUserCount(gu.group.id, COUNT(gu)) FROM UserGroup gu WHERE gu.group.id IN :groupIds GROUP BY gu.group.id")
    List<GroupUserCount> countAllUsersByGroupIds(List<Long> groupIds);

    /**
     * user에 속한 UserGroup 조회
     */
    List<UserGroup> findAllByUser(User user);

    /**
     * 그룹에 해당하는 UserGroup 목록 조회
     */
    @Query("SELECT ug FROM UserGroup ug LEFT JOIN FETCH ug.user WHERE ug.group = :group")
    Page<UserGroup> findAllByGroupId(Group group, Pageable pageable);
}
