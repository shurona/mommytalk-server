package com.shrona.mommytalk.openai.infrastructure.repository.query;

import com.shrona.mommytalk.openai.domain.UserSentenceHistory;
import java.util.List;

public interface UserSentenceHistoryQueryRepository {

    /**
     * 최근의 MessageHistory 정보를 갖고 온다.
     */
    UserSentenceHistory findRecentHistory(Long userId);

    /**
     * 특정 년월의 목록을 갖고 온다.
     */
    List<UserSentenceHistory> findSentenceListByUser(Long userId, Integer year, Integer month);

    /**
     * 오늘(KST 기준) 생성한 문장 개수 조회
     */
    int countTodaySentences(Long userId);

}
