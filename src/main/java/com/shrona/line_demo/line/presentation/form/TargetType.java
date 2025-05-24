package com.shrona.line_demo.line.presentation.form;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TargetType {

    ALL("ALL"),
    GROUP("GROUP");

    private final String type;
}
