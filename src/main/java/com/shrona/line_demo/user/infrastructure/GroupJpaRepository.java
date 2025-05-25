package com.shrona.line_demo.user.infrastructure;

import com.shrona.line_demo.user.domain.Group;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroupJpaRepository extends JpaRepository<Group, Long> {

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.userGroupList ug LEFT JOIN FETCH ug.user WHERE g.id = :id")
    Optional<Group> findGroupWithUsers(Long id);

    List<Group> findByIdNotIn(List<Long> exceptGroupIds);
}
