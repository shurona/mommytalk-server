package com.shrona.mommytalk.line.infrastructure.sender;

import com.shrona.mommytalk.line.infrastructure.sender.dto.LineMessageSingleRequestBody;
import com.shrona.mommytalk.line.infrastructure.sender.dto.LineSendMulticastResponseBody;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface LineMessageSingleSenderClient {

    // 단일 전송
    @PostExchange
    public LineSendMulticastResponseBody sendSingleMessage(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken,
        @RequestBody LineMessageSingleRequestBody requestBody
    );
}
