package com.shrona.mommytalk.user.presentation.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 유저 문장 생성 기록 조회 요청 DTO
 */
public record SentenceHistorySearchRequestDto(
    @NotNull(message = "년도는 필수입니다")
    @Min(value = 2020, message = "년도는 2020년 이상이어야 합니다")
    @Max(value = 2100, message = "년도는 2100년 이하여야 합니다")
    Integer year,

    @NotNull(message = "월은 필수입니다")
    @Min(value = 1, message = "월은 1 이상이어야 합니다")
    @Max(value = 12, message = "월은 12 이하여야 합니다")
    Integer month
) {

    /**
     * 검증된 year, month로 DTO 생성
     */
    public static SentenceHistorySearchRequestDto of(Integer year, Integer month) {
        return new SentenceHistorySearchRequestDto(year, month);
    }
}
