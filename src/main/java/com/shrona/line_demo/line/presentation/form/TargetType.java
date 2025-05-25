package com.shrona.line_demo.line.presentation.form;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TargetType {
    // 메시지 타겟이 (전체, 그룹을 타겟)
    ALL("ALL"),
    GROUP("GROUP");

    private final String type;
}
