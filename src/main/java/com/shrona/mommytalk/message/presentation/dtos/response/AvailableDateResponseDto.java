package com.shrona.mommytalk.message.presentation.dtos.response;

public record AvailableDateResponseDto(
    String date,
    String theme,
    Integer messageCount
) {

    public static AvailableDateResponseDto of(String date, String theme, Integer messageCount) {
        return new AvailableDateResponseDto(date, theme, messageCount);
    }

}
