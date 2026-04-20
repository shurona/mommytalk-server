package com.shrona.mommytalk.cloudflare.application;

import java.io.File;

/**
 * Cloudflare R2 스토리지 서비스 인터페이스
 */
public interface CloudflareService {

    /**
     * 오디오 파일을 R2에 업로드
     *
     * @param audioFile 업로드할 오디오 파일
     * @param fileName  저장할 파일명 (예: "audio_123.mp3")
     * @return 업로드된 파일의 공개 URL
     */
    String uploadAudioFile(File audioFile, String fileName);

    /**
     * 오디오 파일을 R2에 업로드 (커스텀 경로)
     *
     * @param audioFile  업로드할 오디오 파일
     * @param customPath 커스텀 경로 (예: "audio/2024/01/file.mp3")
     * @return 업로드된 파일의 공개 URL
     */
    String uploadAudioFileWithPath(File audioFile, String customPath);

    /**
     * byte[] 오디오 데이터를 R2에 업로드
     *
     * @param audioBytes 업로드할 오디오 데이터
     * @param fileName   저장할 파일명 (예: "audio_123.mp3")
     * @return 업로드된 파일의 공개 URL
     */
    String uploadAudioBytes(byte[] audioBytes, String fileName);

    /**
     * byte[] 오디오 데이터를 R2에 업로드 (커스텀 경로)
     *
     * @param audioBytes 업로드할 오디오 데이터
     * @param customPath 커스텀 경로 (예: "audio/2024/01/file.mp3")
     * @return 업로드된 파일의 공개 URL
     */
    String uploadAudioBytesWithPath(byte[] audioBytes, String customPath);

    /**
     * R2 버킷 내에서 오디오 파일 복사 (서버 사이드)
     *
     * @param sourceFileKey 복사할 원본 파일 경로 (예: "audio/2024/01/file.mp3")
     * @param destFileName  복사본 파일명 (예: "messageContent_102_1234567890.mp3")
     * @return 복사된 파일의 공개 URL
     */
    String copyAudioFile(String sourceFileKey, String destFileName);

    /**
     * R2에서 파일 삭제
     *
     * @param fileKey 삭제할 파일 경로
     */
    void deleteFile(String fileKey);

}
