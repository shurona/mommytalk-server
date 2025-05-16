package com.shrona.line_demo.user.infrastructure;

import com.shrona.line_demo.user.domain.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGroupJpaRepository extends JpaRepository<UserGroup, Long> {

}
