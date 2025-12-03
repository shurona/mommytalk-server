package com.shrona.mommytalk.message.presentation.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BulkImportMessageRequestDto(
    @NotNull(message = "날짜는 필수입니다")
    @JsonFormat(pattern = "yyyy-M-d")
    LocalDate date,

    @NotBlank(message = "제목은 필수입니다")
    String title,
    
    String contents,

    String link  // mommyVoca (optional)
) {

}
