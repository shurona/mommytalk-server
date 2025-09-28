package com.shrona.mommytalk.message.presentation.dtos.response;

import static lombok.AccessLevel.PRIVATE;

import lombok.Builder;

@Builder(access = PRIVATE)
public record UpdateContentResponseDto(
    String message,
    UpdateContentData data
) {

    @Builder(access = PRIVATE)
    public record UpdateContentData(
        boolean success,
        String message
    ) {

        public static UpdateContentData createSuccess() {
            return UpdateContentData.builder()
                .success(true)
                .message("콘텐츠가 성공적으로 수정되었습니다.")
                .build();
        }

        public static UpdateContentData createApproved() {
            return UpdateContentData.builder()
                .success(true)
                .message("콘텐츠가 승인되었습니다.")
                .build();
        }
    }

    public static UpdateContentResponseDto success() {
        return UpdateContentResponseDto.builder()
            .message("Success")
            .data(UpdateContentData.createSuccess())
            .build();
    }

    public static UpdateContentResponseDto approved() {
        return UpdateContentResponseDto.builder()
            .message("Success")
            .data(UpdateContentData.createApproved())
            .build();
    }
}