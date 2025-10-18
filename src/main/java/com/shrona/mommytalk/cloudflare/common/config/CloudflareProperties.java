package com.shrona.mommytalk.cloudflare.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudflare.r2")
public record CloudflareProperties(
    String accountId,
    String accessKeyId,
    String secretAccessKey,
    String bucketName,
    String endpoint,
    String publicUrl
) {


}
