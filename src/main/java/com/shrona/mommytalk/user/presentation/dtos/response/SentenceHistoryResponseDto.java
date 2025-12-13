package com.shrona.mommytalk.user.presentation.dtos.response;

import java.time.LocalDate;

/**
 * 유저 생성 문장 이력 응답 DTO
 * (관리자용 - 채널별 유저들의 문장 생성 이력 조회)
 */
public record SentenceHistoryResponseDto(
    Long userId,
    Long userSentenceHistoryId,
    LocalDate generateDate,
    String sentence,
    String output,
    Integer userLevel,
    Integer childLevel,
    Integer completionTokens,
    Integer totalTokens
) {

    /**
     * QueryDSL Projection 결과로부터 DTO 생성
     */
    public static SentenceHistoryResponseDto of(
        Long userId,
        Long userSentenceHistoryId,
        LocalDate generateDate,
        String sentence,
        String output,
        Integer userLevel,
        Integer childLevel,
        Integer completionTokens,
        Integer totalTokens
    ) {
        return new SentenceHistoryResponseDto(
            userId,
            userSentenceHistoryId,
            generateDate,
            sentence,
            output,
            userLevel,
            childLevel,
            completionTokens,
            totalTokens
        );
    }
}
