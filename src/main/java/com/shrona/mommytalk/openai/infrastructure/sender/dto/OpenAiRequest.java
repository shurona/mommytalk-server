package com.shrona.mommytalk.openai.infrastructure.sender.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OpenAiRequest {

    private String model;

    private List<Message> messages;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private Double temperature;

    @Getter
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}