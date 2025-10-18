package com.shrona.mommytalk.elevenlabs.infrastructure.adapter;

import com.shrona.mommytalk.elevenlabs.infrastructure.sender.ElevenLabsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * ElevenLabs API RestClient 설정
 */
@Configuration
public class ElevenLabsAdapter {

    @Value("${elevenlabs.base-url}")
    private String baseUrl;

    @Bean
    public ElevenLabsClient elevenLabsClient() {
        RestClient restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(ElevenLabsClient.class);
    }
}
