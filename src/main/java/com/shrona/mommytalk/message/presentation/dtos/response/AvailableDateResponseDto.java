package com.shrona.mommytalk.message.presentation.dtos.response;

public record AvailableDateResponseDto(
    String date,
    Integer messageCount
) {

    public static AvailableDateResponseDto of(String date, Integer ct) {
        return new AvailableDateResponseDto(date, ct);
    }

}
