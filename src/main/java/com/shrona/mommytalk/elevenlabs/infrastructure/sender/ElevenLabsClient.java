package com.shrona.mommytalk.elevenlabs.infrastructure.sender;

import com.shrona.mommytalk.elevenlabs.infrastructure.sender.dto.ElevenLabsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * ElevenLabs Text-to-Speech API 클라이언트
 */
@HttpExchange
public interface ElevenLabsClient {

    /**
     * 텍스트를 음성으로 변환
     */
    @PostExchange("/v1/text-to-speech/{voiceId}")
    ResponseEntity<byte[]> textToSpeech(
        @PathVariable String voiceId,
        @RequestBody ElevenLabsRequest request,
        @RequestHeader("xi-api-key") String apiKey
    );
}
