package com.shrona.mommytalk.entitlement.presentation.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BulkUpdateUserEntitlementRequestDto(
    @NotBlank(message = "휴대전화 번호는 필수입니다")
    String phoneNumber,

    @NotNull(message = "시작 날짜는 필수입니다")
    @JsonFormat(pattern = "yyyy-M-d")
    LocalDate startDate,

    @NotNull(message = "종료 날짜는 필수입니다")
    @JsonFormat(pattern = "yyyy-M-d")
    LocalDate endDate
) {
}
