package com.shrona.mommytalk.message.presentation.dtos.response;

import static lombok.AccessLevel.PRIVATE;

import lombok.Builder;

@Builder(access = PRIVATE)
public record ContentStatusResponseDto(
    String message,
    ContentStatusData data
) {

    @Builder(access = PRIVATE)
    public record ContentStatusData(
        int generatedCount,
        int approvedCount
    ) {

        public static ContentStatusData of(int generatedCount, int approvedCount) {
            return ContentStatusData.builder()
                .generatedCount(generatedCount)
                .approvedCount(approvedCount)
                .build();
        }
    }

    public static ContentStatusResponseDto of(int generatedCount, int approvedCount) {
        return ContentStatusResponseDto.builder()
            .message("Success")
            .data(ContentStatusData.of(generatedCount, approvedCount))
            .build();
    }
}