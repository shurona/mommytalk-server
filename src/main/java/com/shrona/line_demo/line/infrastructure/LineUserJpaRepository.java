package com.shrona.line_demo.line.infrastructure;

import com.shrona.line_demo.line.domain.LineUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineUserJpaRepository extends JpaRepository<LineUser, Long> {

    Optional<LineUser> findByLineId(String lineId);
}
