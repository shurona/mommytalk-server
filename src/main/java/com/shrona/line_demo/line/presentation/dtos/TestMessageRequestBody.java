package com.shrona.line_demo.line.presentation.dtos;

public record TestMessageRequestBody(
    String text,
    String headerLink,
    String footerLink
) {

}
