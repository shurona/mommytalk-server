package com.shrona.mommytalk.linehook.infrastructure;

import com.shrona.mommytalk.line.domain.LineMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineMessageJpaRepository extends JpaRepository<LineMessage, Long> {


}
