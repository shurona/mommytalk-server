package com.shrona.mommytalk.openai.infrastructure.sender;

import com.shrona.mommytalk.openai.infrastructure.sender.dto.OpenAiRequest;
import com.shrona.mommytalk.openai.infrastructure.sender.dto.OpenAiResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface OpenAiClient {

    @PostExchange("/chat/completions")
    OpenAiResponse sendChatCompletion(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("Content-Type") String contentType,
        @RequestBody OpenAiRequest request
    );
}
