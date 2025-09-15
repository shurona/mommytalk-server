package com.shrona.mommytalk.group.infrastructure.repository;

import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.domain.UserGroup;
import com.shrona.mommytalk.group.infrastructure.dao.GroupUserCount;
import com.shrona.mommytalk.user.domain.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserGroupJpaRepository extends JpaRepository<UserGroup, Long> {

    /**
     * 전송을 위한 user의 count 조회
     */
    @Query("SELECT new com.shrona.mommytalk.group.infrastructure.dao.GroupUserCount(gu.group.id, COUNT(gu)) FROM UserGroup gu WHERE gu.group.id IN :groupIds AND gu.user.lineUser is not null GROUP BY gu.group.id")
    List<GroupUserCount> countByGroupIds(List<Long> groupIds);

    /**
     * 전송을 위한 user의 count 조회
     */
    @Query("SELECT new com.shrona.mommytalk.group.infrastructure.dao.GroupUserCount(gu.group.id, COUNT(gu)) FROM UserGroup gu WHERE gu.group.id IN :groupIds GROUP BY gu.group.id")
    List<GroupUserCount> countAllUsersByGroupIds(List<Long> groupIds);

    /**
     * user에 속한 UserGroup 조회
     */
    List<UserGroup> findAllByUser(User user);

    /**
     * 그룹에 해당하는 UserGroup 목록 조회
     */
    @Query("""
        SELECT ug FROM UserGroup ug
        LEFT JOIN FETCH ug.user u
        LEFT JOIN FETCH u.lineUser lu
        WHERE ug.group = :group
        """)
    Page<UserGroup> findAllByGroupId(Group group, Pageable pageable);

    @Modifying
    @Query(value = "DELETE FROM user_group WHERE user_id = :userId", nativeQuery = true)
    void deleteAllByUserIdWithoutRestriction(Long userId);

    @Modifying
    @Query(value = "DELETE FROM user_group WHERE user_id IN :userIds", nativeQuery = true)
    void deleteAllByUserId(List<Long> userIds);
}
