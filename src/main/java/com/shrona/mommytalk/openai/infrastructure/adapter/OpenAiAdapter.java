package com.shrona.mommytalk.openai.infrastructure.adapter;

import com.shrona.mommytalk.openai.infrastructure.sender.OpenAiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
public class OpenAiAdapter {

    private final String openAiApiUrl;

    public OpenAiAdapter(@Value("${openai.base-url}") String openAiBaseUrl) {
        this.openAiApiUrl = openAiBaseUrl;
    }

    /**
     * OpenAi 클라이언트
     */
    @Bean
    public OpenAiClient openAiClient() {

        RestClient restClient = RestClient.builder()
            .baseUrl(openAiApiUrl)
//            .requestInterceptor(logRequestInterceptor())
            .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        // 어댑터를 기반으로 HTTP 서비스 프록시 팩토리를 빌드
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(OpenAiClient.class);
    }
}
