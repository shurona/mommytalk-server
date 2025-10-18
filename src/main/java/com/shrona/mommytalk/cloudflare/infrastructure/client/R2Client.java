package com.shrona.mommytalk.cloudflare.infrastructure.client;

import java.io.File;

/**
 * Cloudflare R2 스토리지 클라이언트 인터페이스
 */
public interface R2Client {

    /**
     * 파일을 R2 스토리지에 업로드
     *
     * @param file 업로드할 파일
     * @param key 저장될 파일 경로 (예: "audio/2024/01/file.mp3")
     * @return 업로드된 파일의 공개 URL
     */
    String uploadFile(File file, String key);

    /**
     * 파일을 R2 스토리지에 업로드 (contentType 지정)
     *
     * @param file 업로드할 파일
     * @param key 저장될 파일 경로
     * @param contentType MIME 타입 (예: "audio/mpeg")
     * @return 업로드된 파일의 공개 URL
     */
    String uploadFile(File file, String key, String contentType);

    /**
     * byte[] 데이터를 R2 스토리지에 업로드
     *
     * @param bytes 업로드할 데이터
     * @param key 저장될 파일 경로 (예: "audio/2024/01/file.mp3")
     * @param contentType MIME 타입 (예: "audio/mpeg")
     * @return 업로드된 파일의 공개 URL
     */
    String uploadBytes(byte[] bytes, String key, String contentType);

    /**
     * R2에서 파일 삭제
     *
     * @param key 삭제할 파일 경로
     */
    void deleteFile(String key);

}
