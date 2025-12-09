package com.shrona.mommytalk.message.presentation.dtos.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UploadLegacyAudioRequestDto(
    @NotNull(message = "연도는 필수입니다")
    @Min(value = 2025, message = "연도는 2025 이상이어야 합니다")
    Integer year,

    @NotNull(message = "월은 필수입니다")
    @Min(value = 1, message = "월은 1~12 사이여야 합니다")
    @Max(value = 12, message = "월은 1~12 사이여야 합니다")
    Integer month
) {
}
