package com.shrona.mommytalk.user.presentation.dtos.response;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record UserSentenceListResponseDto(
    Long id,
    String sentence,
    String output,
    LocalDate generateDate
) {

    public static UserSentenceListResponseDto of(
        Long id, String sentence, String output, LocalDate generateDate
    ) {
        return UserSentenceListResponseDto.builder()
            .id(id)
            .sentence(sentence)
            .output(output)
            .generateDate(generateDate)
            .build();
    }

}
