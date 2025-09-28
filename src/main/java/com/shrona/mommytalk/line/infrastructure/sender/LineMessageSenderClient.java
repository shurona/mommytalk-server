package com.shrona.mommytalk.line.infrastructure.sender;

import com.shrona.mommytalk.line.infrastructure.sender.dto.LineMessageMulticastRequestBody;
import com.shrona.mommytalk.line.infrastructure.sender.dto.LineMessageSingleRequestBody;
import com.shrona.mommytalk.line.infrastructure.sender.dto.LineSendMulticastResponseBody;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface LineMessageSenderClient {

    // 여러 명에게 전송
    @PostExchange("/multicast")
    public LineSendMulticastResponseBody SendMulticastMessage(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken,
        @RequestBody LineMessageMulticastRequestBody requestBody
    );


    // 단일 전송
    @PostExchange("/push")
    public LineSendMulticastResponseBody sendSingleMessage(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken,
        @RequestBody LineMessageSingleRequestBody requestBody
    );

}
