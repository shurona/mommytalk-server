package com.shrona.mommytalk.auth.infrastructure.adapter;

import com.shrona.mommytalk.auth.infrastructure.sender.LineAuthClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
public class LineAuthAdapter {

    private static final String LINE_API_BASE_URL = "https://api.line.me";

    /**
     * LINE Auth용 RestClient (form data 전송용)
     */
    @Bean
    public RestClient lineAuthRestClient() {
        return RestClient.builder()
            .baseUrl(LINE_API_BASE_URL)
            .build();
    }

    /**
     * LINE Auth 클라이언트 (프로필 조회용)
     */
    @Bean
    public LineAuthClient lineAuthClient() {

        RestClient restClient = RestClient.builder()
            .baseUrl(LINE_API_BASE_URL)
            .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        // 어댑터를 기반으로 HTTP 서비스 프록시 팩토리를 빌드
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(LineAuthClient.class);
    }
}
