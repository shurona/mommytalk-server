package com.shrona.mommytalk.openai.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PromptType {
    BASIC("BASIC"),
    ADVANCE("ADVANCE"),
    ;


    private final String type;
}
