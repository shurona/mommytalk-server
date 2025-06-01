package com.shrona.line_demo.linehook.infrastructure;

import com.shrona.line_demo.line.domain.LineMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineMessageJpaRepository extends JpaRepository<LineMessage, Long> {


}
