package com.shrona.mommytalk.openai.infrastructure.repository.query;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.openai.domain.UserSentenceHistory;
import com.shrona.mommytalk.user.presentation.dtos.response.SentenceHistoryResponseDto;
import com.shrona.mommytalk.user.presentation.dtos.response.TokenUsageResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    int countTodaySentences(Long userId, Channel channel);

    /**
     * 채널별 유저 문장 이력 조회 (관리자용)
     * - 선택적 날짜 필터링 (year, month, day)
     * - 선택적 유저 필터링 (userId)
     * - 페이징 지원
     * - User, UserSentenceHistoryUsageLog 조인
     */
    Page<SentenceHistoryResponseDto> findSentenceHistoryByChannel(
        Long channelId,
        Integer year,
        Integer month,
        Integer day,
        Long userId,
        Pageable pageable
    );

    /**
     * 채널별 토큰 사용량 합계 조회 (관리자용)
     * - 선택적 날짜 필터링 (year, month, day)
     * - 선택적 유저 필터링 (userId)
     * - completionTokens, totalTokens 합계 반환
     */
    TokenUsageResponseDto getTokenUsageSumByChannel(
        Long channelId,
        Integer year,
        Integer month,
        Integer day,
        Long userId
    );

}
