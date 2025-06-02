package com.shrona.line_demo.user.infrastructure;

import com.shrona.line_demo.user.domain.User;
import com.shrona.line_demo.user.domain.UserGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserGroupJpaRepository extends JpaRepository<UserGroup, Long> {

    /**
     * 전송을 위한 user의 count 조회
     */
    @Query("SELECT gu.group.id, COUNT(gu) FROM UserGroup gu WHERE gu.group.id IN :groupIds AND gu.user.lineId is not null GROUP BY gu.group.id")
    List<Object[]> countByGroupIds(List<Long> groupIds);

    /**
     * user에 속한 UserGroup 조회
     */
    List<UserGroup> findAllByUser(User user);

}
