package com.shrona.mommytalk.cloudflare.common.config;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.retries.StandardRetryStrategy;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class R2Config {

    private final CloudflareProperties properties;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
            properties.accessKeyId(),
            properties.secretAccessKey()
        );

        return S3Client.builder()
            .endpointOverride(URI.create(properties.endpoint()))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of("auto"))
            .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
            .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
            .serviceConfiguration(S3Configuration.builder()
                .pathStyleAccessEnabled(true) // R2는 path-style access 사용
                .build())
            .overrideConfiguration(ClientOverrideConfiguration.builder()
                .retryStrategy(StandardRetryStrategy.builder()
                    .maxAttempts(5) // 초기 시도 1회 + 재시도 4회 (네트워크 불안정 대비)
                    .build())
                .build())
            .build();
    }

}
