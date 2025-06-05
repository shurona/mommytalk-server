package com.shrona.line_demo.common.dto;

public record PagingForm(
    int number,
    int totalPages,
    boolean hasPrevious,
    boolean hasNext

) {

    public static PagingForm of(int pageNumber, int totalPages) {
        totalPages = (totalPages == 0) ? 1 : totalPages;
        // Front에서 페이지는 1부터 사작하므로 수정해준다.
        return new PagingForm(
            pageNumber,
            totalPages,
            pageNumber != 0,
            (pageNumber + 1) != totalPages // 아무 페이지도 없거나 최종페이지 확인
        );
    }

}
