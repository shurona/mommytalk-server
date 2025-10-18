package com.shrona.mommytalk.cloudflare.application;

import com.shrona.mommytalk.cloudflare.common.exception.CloudflareErrorCode;
import com.shrona.mommytalk.cloudflare.common.exception.CloudflareException;
import com.shrona.mommytalk.cloudflare.infrastructure.client.R2Client;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudflareServiceImpl implements CloudflareService {

    private static final String AUDIO_BASE_PATH = "audio";
    private static final String AUDIO_CONTENT_TYPE = "audio/mpeg";
    private static final DateTimeFormatter PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");

    private final R2Client r2Client;

    @Override
    public String uploadAudioFile(File audioFile, String fileName) {
        validateFile(audioFile);

        // 날짜별 경로 생성: audio/2024/01/filename.mp3
        String datePath = LocalDateTime.now().format(PATH_FORMATTER);
        String fileKey = String.format("%s/%s/%s", AUDIO_BASE_PATH, datePath, fileName);

        return uploadWithKey(audioFile, fileKey);
    }

    @Override
    public String uploadAudioFileWithPath(File audioFile, String customPath) {
        validateFile(audioFile);
        return uploadWithKey(audioFile, customPath);
    }

    @Override
    public String uploadAudioBytes(byte[] audioBytes, String fileName) {
        validateBytes(audioBytes);

        // 날짜별 경로 생성: audio/2024/01/filename.mp3
        String datePath = LocalDateTime.now().format(PATH_FORMATTER);
        String fileKey = String.format("%s/%s/%s", AUDIO_BASE_PATH, datePath, fileName);

        return uploadBytesWithKey(audioBytes, fileKey);
    }

    @Override
    public String uploadAudioBytesWithPath(byte[] audioBytes, String customPath) {
        validateBytes(audioBytes);
        return uploadBytesWithKey(audioBytes, customPath);
    }

    @Override
    public void deleteFile(String fileKey) {
        try {
            r2Client.deleteFile(fileKey);
            log.info("Successfully deleted file from R2: {}", fileKey);
        } catch (Exception e) {
            log.error("Failed to delete file from R2: {}", fileKey, e);
            throw e;
        }
    }

    /**
     * 파일 검증
     */
    private void validateFile(File file) {
        if (file == null) {
            throw new CloudflareException(CloudflareErrorCode.INVALID_FILE);
        }
        if (!file.exists()) {
            throw new CloudflareException(CloudflareErrorCode.FILE_NOT_FOUND);
        }
        if (!file.isFile()) {
            throw new CloudflareException(CloudflareErrorCode.INVALID_FILE);
        }
    }

    /**
     * byte[] 검증
     */
    private void validateBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new CloudflareException(CloudflareErrorCode.INVALID_FILE);
        }
    }

    /**
     * 파일 키로 업로드
     */
    private String uploadWithKey(File audioFile, String fileKey) {
        try {
            String publicUrl = r2Client.uploadFile(audioFile, fileKey, AUDIO_CONTENT_TYPE);
            log.info("Successfully uploaded audio file to R2: {} -> {}", audioFile.getName(), publicUrl);
            return publicUrl;
        } catch (CloudflareException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while uploading audio file: {}", audioFile.getName(), e);
            throw new CloudflareException(CloudflareErrorCode.UPLOAD_FAILED, e);
        }
    }

    /**
     * byte[] 키로 업로드
     */
    private String uploadBytesWithKey(byte[] audioBytes, String fileKey) {
        try {
            String publicUrl = r2Client.uploadBytes(audioBytes, fileKey, AUDIO_CONTENT_TYPE);
            log.info("Successfully uploaded audio bytes to R2: {} ({} bytes)", publicUrl, audioBytes.length);
            return publicUrl;
        } catch (CloudflareException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while uploading audio bytes: {} bytes", audioBytes.length, e);
            throw new CloudflareException(CloudflareErrorCode.UPLOAD_FAILED, e);
        }
    }

}
