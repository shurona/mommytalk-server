package com.shrona.mommytalk.openai.infrastructure.repository.jpa;

import com.shrona.mommytalk.openai.domain.UserSentenceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSentenceHistoryJpaRepository extends JpaRepository<UserSentenceHistory, Long> {

}
