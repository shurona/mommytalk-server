package com.shrona.mommytalk.openai.infrastructure.repository.jpa;

import com.shrona.mommytalk.openai.domain.OpenAiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpenAiUsageLogJpaRepository extends JpaRepository<OpenAiUsageLog, Long> {

}
