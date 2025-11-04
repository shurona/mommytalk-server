package com.shrona.mommytalk.openai.infrastructure.repository.jpa;

import com.shrona.mommytalk.openai.domain.UserSentenceHistoryUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSentenceHistoryUsageLogRepository extends
    JpaRepository<UserSentenceHistoryUsageLog, Long> {

}
