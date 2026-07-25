package com.shrona.mommytalk.elevenlabs.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "elevenlabs")
public record ElevenlabsConfig(
    String apiKey,
    String modelId,
    String outputDir
) {

}
