package com.shrona.mommytalk.line.presentation.controller;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.line.application.ChannelService;
import com.shrona.mommytalk.line.application.sender.MessageSender;
import com.shrona.mommytalk.line.presentation.dtos.TestMessageRequestBody;
import com.shrona.mommytalk.line.presentation.dtos.TestMessageResponseBody;
import java.util.Optional;
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
    private final ChannelService channelService;

    @PostMapping("/test")
    public ResponseEntity<TestMessageResponseBody> testDeliveryMessage(
        @RequestBody TestMessageRequestBody requestBody
    ) {
        Optional<Channel> channel = channelService.findChannelById(1L);
        if (channel.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        boolean b = messageSender.sendTestLineMessage(channel.get(), requestBody.text());

        return ResponseEntity.ok().body(new TestMessageResponseBody(b));
    }


}
