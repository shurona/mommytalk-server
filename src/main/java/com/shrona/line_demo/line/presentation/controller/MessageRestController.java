package com.shrona.line_demo.line.presentation.controller;

import com.shrona.line_demo.line.application.sender.MessageSender;
import com.shrona.line_demo.line.presentation.dtos.TestMessageRequestBody;
import com.shrona.line_demo.line.presentation.dtos.TestMessageResponseBody;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//local 테스트 용 Controller
@Profile({"local"})
@RequiredArgsConstructor
@RequestMapping("/api/v1/messages")
@RestController
public class MessageRestController {

    private final MessageSender messageSender;

    @PostMapping("/test")
    public ResponseEntity<TestMessageResponseBody> testDeliveryMessage(
        @RequestBody TestMessageRequestBody requestBody
    ) {

        boolean b = messageSender.sendTestLineMessage(requestBody.text());

        return ResponseEntity.ok().body(new TestMessageResponseBody(b));
    }


}
