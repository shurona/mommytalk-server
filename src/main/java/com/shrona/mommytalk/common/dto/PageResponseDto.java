package com.shrona.mommytalk.common.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponseDto<T>(
    List<T> content,
    int page, // 현재 페이지
    int size, // 현재 사이즈
    long totalElements, // 총 갯수
    int totalPages // 총 페이지
) {

    public static <T> PageResponseDto<T> from(Page<T> dataList) {
        return new PageResponseDto<>(
            dataList.toList(),
            dataList.getNumber(),
            dataList.getSize(),
            dataList.getTotalElements(),
            dataList.getTotalPages());
    }

}
