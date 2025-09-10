package com.shrona.mommytalk.common.dto;

public record ApiResponse<T>(
    String message,
    T data
) {


    /**
     * 성공 시 메서드
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("Success", data);
    }

    /**
     * 실패시 메서드 - data 필드가 필요 없는 경우
     */
    public static ApiResponse<Void> error(String message) {

        return new ApiResponse<>(message, null);
    }

    /**
     * 실패시 메서드 - data 필드가 필요한 경우
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(message, data);
    }

}

