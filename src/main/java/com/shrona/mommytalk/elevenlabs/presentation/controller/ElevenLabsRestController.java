package com.shrona.mommytalk.elevenlabs.presentation.controller;

import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.elevenlabs.application.ElevenLabsService;
import com.shrona.mommytalk.elevenlabs.infrastructure.sender.dto.ElevenLabsRequest;
import com.shrona.mommytalk.elevenlabs.presentation.dtos.request.GenerateAudioRequestDto;
import com.shrona.mommytalk.elevenlabs.presentation.dtos.response.GenerateAudioResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ElevenLabs TTS API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/elevenlabs")
@RequiredArgsConstructor
@Slf4j
public class ElevenLabsRestController {

    private final ElevenLabsService elevenLabsService;

    /**
     * 텍스트를 음성으로 변환하여 파일 생성
     */
    @PostMapping("/generate")
    public ApiResponse<GenerateAudioResponseDto> generateAudio(
        @RequestBody GenerateAudioRequestDto requestDto
    ) {
        log.info("오디오 생성 요청 - messageContentId: {}, text length: {}",
            requestDto.messageContentId(), requestDto.text().length());

        ElevenLabsRequest elevenLabsRequest = requestDto.toElevenLabsRequest();
        String filePath = elevenLabsService.generateAudio(
            elevenLabsRequest,
            requestDto.messageContentId()
        ).getFileUrl();

        GenerateAudioResponseDto response = GenerateAudioResponseDto.of(
            requestDto.messageContentId(),
            filePath
        );

        return ApiResponse.success(response);
    }
}
