package com.shrona.line_demo.admin.infrastructure;

import com.shrona.line_demo.admin.domain.AdminUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminJpaRepository extends JpaRepository<AdminUser, Long> {


    Optional<AdminUser> findByLoginId(String loginId);
}
