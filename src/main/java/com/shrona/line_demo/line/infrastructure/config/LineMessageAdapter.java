package com.shrona.line_demo.line.infrastructure.config;

import com.shrona.line_demo.line.infrastructure.sender.LineMessageSenderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class LineMessageAdapter {

    private final String lineBaseUrl;
    private final String lineAccessKey;


    public LineMessageAdapter(@Value("line.base-url") String lineBaseUrl,
        @Value("line.access-token") String lineAccessKey) {
        this.lineBaseUrl = lineBaseUrl;
        this.lineAccessKey = lineAccessKey;
    }

    @Bean
    public LineMessageSenderClient LineMessageMultiCastClient() {

        RestClient restClient = RestClient.builder()
            .baseUrl(lineBaseUrl + "multicast")
            .defaultHeaders(headers -> {
                headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + lineAccessKey);
            })
            .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        // 어댑터를 기반으로 HTTP 서비스 프록시 팩토리를 빌드
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(LineMessageSenderClient.class);
    }

}
