package com.shrona.line_demo.common.dto;

public record PagingForm(
    int number,
    int totalPages,
    boolean hasPrevious,
    boolean hasNext,
    String requestUrl

) {

    public static PagingForm of(int pageNumber, int totalPages, String requestUrl) {
        // Front에서 페이지는 1부터 사작하므로 수정해준다.
        return new PagingForm(
            pageNumber,
            totalPages,
            pageNumber != 0,
            (pageNumber + 1) != totalPages,
            requestUrl);
    }

}
