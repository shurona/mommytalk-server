package com.shrona.mommytalk.elevenlabs.application;

import com.shrona.mommytalk.cloudflare.application.CloudflareService;
import com.shrona.mommytalk.elevenlabs.common.config.ElevenlabsConfig;
import com.shrona.mommytalk.elevenlabs.domain.ElevenLabsMedia;
import com.shrona.mommytalk.elevenlabs.infrastructure.reposiotry.ElevenLabsMediaRepository;
import com.shrona.mommytalk.elevenlabs.infrastructure.sender.ElevenLabsClient;
import com.shrona.mommytalk.elevenlabs.infrastructure.sender.dto.ElevenLabsRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * ElevenLabs TTS 서비스 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ElevenLabsServiceImpl implements ElevenLabsService {

    private final ElevenLabsClient elevenLabsClient;
    private final CloudflareService cloudflareService;

    private final ElevenlabsConfig elevenlabsConfig;

    private final ElevenLabsMediaRepository elevenLabsMediaRepository;

    @Override
    public String generateAudio(String text, Long messageContentId) {
        ElevenLabsRequest request = ElevenLabsRequest.of(text);
        return generateAudio(request, messageContentId, elevenlabsConfig.voiceId()).getFileUrl();
    }

    @Override
    public ElevenLabsMedia generateAudio(
        ElevenLabsRequest request, Long messageContentId, String voiceId) {
        try {
            log.info("ElevenLabs TTS 시작 - messageContentId: {}", messageContentId);

            // API 호출
            ResponseEntity<byte[]> response = elevenLabsClient.textToSpeech(
                elevenlabsConfig.voiceId(),
                request,
                elevenlabsConfig.apiKey()
            );

            byte[] audioData = response.getBody();
            if (audioData == null || audioData.length == 0) {
                throw new RuntimeException("ElevenLabs API 응답이 비어있습니다.");
            }

            // R2에 업로드 (파일명: messageContent_{id}_{timestamp}.mp3)
            String fileName = String.format("messageContent_%d_%d.mp3", messageContentId,
                System.currentTimeMillis());
            String publicUrl = cloudflareService.uploadAudioBytes(audioData, fileName);

            // 미디어 정보 저장

            log.info("ElevenLabs TTS 완료 - R2 URL: {}", publicUrl);
            return elevenLabsMediaRepository.save(
                ElevenLabsMedia.of(request.text(), publicUrl, fileName, audioData.length)
            );

        } catch (Exception e) {
            log.error("ElevenLabs API 호출 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("오디오 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 로컬 테스트용 저장 로직
     */
    public void generateAudioInLocal(Long messageContentId, byte[] audioData) {

        try {
            Path dirPath = Paths.get(elevenlabsConfig.outputDir());
            // 디렉토리 생성
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.info("디렉토리 생성: {}", dirPath.toAbsolutePath());
            }

            // 파일 저장
            String fileName = String.format("audio_%d.mp3", messageContentId);
            Path filePath = dirPath.resolve(fileName);
            Files.write(filePath, audioData);

            String absolutePath = filePath.toAbsolutePath().toString();
            log.info("오디오 파일 저장 완료: {}", absolutePath);
            log.info("파일 크기: {} bytes", audioData.length);
        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("오디오 파일 저장 실패: " + e.getMessage(), e);
        }

    }
}
