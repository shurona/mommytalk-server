package com.shrona.mommytalk.cloudflare.presentation.controller;

import com.shrona.mommytalk.cloudflare.application.CloudflareService;
import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/cloudflare")
@RequiredArgsConstructor
public class CloudflareTestController {

    private final CloudflareService cloudflareService;

    /**
     * 로컬 파일 경로로 오디오 업로드 테스트
     *
     * @param filePath 로컬 파일 절대 경로
     * @return 업로드된 파일의 공개 URL
     */
    @PostMapping("/test-upload")
    public String testUpload(@RequestParam String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();

        String publicUrl = cloudflareService.uploadAudioFile(file, fileName);
        log.info("Test upload successful: {}", publicUrl);

        return publicUrl;
    }

}
