package com.shrona.mommytalk.cloudflare.infrastructure.client;

import com.shrona.mommytalk.cloudflare.common.config.CloudflareProperties;
import com.shrona.mommytalk.cloudflare.common.exception.CloudflareErrorCode;
import com.shrona.mommytalk.cloudflare.common.exception.CloudflareException;
import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Component
@RequiredArgsConstructor
public class R2ClientImpl implements R2Client {

    private final S3Client s3Client;
    private final CloudflareProperties properties;

    @Override
    public String uploadFile(File file, String key) {
        return uploadFile(file, key, "application/octet-stream");
    }

    @Override
    public String uploadFile(File file, String key, String contentType) {
        if (file == null || !file.exists()) {
            throw new CloudflareException(CloudflareErrorCode.FILE_NOT_FOUND);
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(key)
                .contentType(contentType)
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));

            // 공개 URL 생성
            String publicUrl = buildPublicUrl(key);
            log.info("File uploaded successfully to R2: {}", publicUrl);

            return publicUrl;

        } catch (S3Exception e) {
            log.error("S3 error while uploading file: {}", e.getMessage(), e);
            throw new CloudflareException(CloudflareErrorCode.S3_CLIENT_ERROR, e);
        } catch (Exception e) {
            log.error("Failed to upload file to R2: {}", e.getMessage(), e);
            throw new CloudflareException(CloudflareErrorCode.UPLOAD_FAILED, e);
        }
    }

    @Override
    public String uploadBytes(byte[] bytes, String key, String contentType) {
        if (bytes == null || bytes.length == 0) {
            throw new CloudflareException(CloudflareErrorCode.INVALID_FILE);
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(key)
                .contentType(contentType)
                .contentLength((long) bytes.length)
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

            // 공개 URL 생성
            String publicUrl = buildPublicUrl(key);

            return publicUrl;

        } catch (S3Exception e) {
            log.error("S3 error while uploading bytes: {}", e.getMessage(), e);
            throw new CloudflareException(CloudflareErrorCode.S3_CLIENT_ERROR, e);
        } catch (Exception e) {
            log.error("Failed to upload bytes to R2: {}", e.getMessage(), e);
            throw new CloudflareException(CloudflareErrorCode.UPLOAD_FAILED, e);
        }
    }

    @Override
    public String copyFile(String sourceKey, String destKey) {
        try {
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(properties.bucketName())
                .sourceKey(sourceKey)
                .destinationBucket(properties.bucketName())
                .destinationKey(destKey)
                .build();

            s3Client.copyObject(copyRequest);
            String publicUrl = buildPublicUrl(destKey);
            log.info("File copied in R2: {} -> {}", sourceKey, publicUrl);
            return publicUrl;

        } catch (S3Exception e) {
            log.error("S3 error while copying file: {}", e.getMessage(), e);
            throw new CloudflareException(CloudflareErrorCode.S3_CLIENT_ERROR, e);
        } catch (Exception e) {
            log.error("Failed to copy file in R2: {}", e.getMessage(), e);
            throw new CloudflareException(CloudflareErrorCode.UPLOAD_FAILED, e);
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(key)
                .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from R2: {}", key);

        } catch (S3Exception e) {
            log.error("S3 error while deleting file: {}", e.getMessage(), e);
            throw new CloudflareException(CloudflareErrorCode.S3_CLIENT_ERROR, e);
        } catch (Exception e) {
            log.error("Failed to delete file from R2: {}", e.getMessage(), e);
            throw new CloudflareException(CloudflareErrorCode.UPLOAD_FAILED, e);
        }
    }

    /**
     * 공개 URL 생성
     */
    private String buildPublicUrl(String key) {
        // public-url이 설정되어 있으면 사용, 아니면 endpoint 사용
        String baseUrl = properties.publicUrl() != null
            ? properties.publicUrl()
            : String.format("https://%s.r2.cloudflarestorage.com/%s",
                properties.accountId(), properties.bucketName());

        return String.format("%s/%s", baseUrl, key);
    }

}
