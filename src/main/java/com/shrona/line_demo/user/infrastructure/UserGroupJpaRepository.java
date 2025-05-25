package com.shrona.line_demo.user.infrastructure;

import com.shrona.line_demo.user.domain.UserGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserGroupJpaRepository extends JpaRepository<UserGroup, Long> {

    @Query("SELECT gu.group.id, COUNT(gu) FROM UserGroup gu WHERE gu.group.id IN :groupIds GROUP BY gu.group.id")
    List<Object[]> countByGroupIds(List<Long> groupIds);

}
