package com.shrona.mommytalk.openai.infrastructure.sender.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OpenAiRequest {

    private String model;

    private List<Message> messages;

    @JsonProperty("max_completion_tokens")
    private Integer maxTokens;

    private Double temperature;

    @Getter
    @Builder
    public static class Message {

        private String role;
        private String content;
    }
}