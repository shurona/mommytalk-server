package com.shrona.mommytalk.kakao.infrastructure.adapter;

import com.shrona.mommytalk.kakao.infrastructure.sender.NhnKakaoMessageClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
public class KakaoMessageAdapter {

    private final String kakaoBaseUrl;
    private final String kakaoAppKey;

    public KakaoMessageAdapter(
        @Value("${kakao.base-url}") String kakaoBaseUrl,
        @Value("${kakao.app-key}") String kakaoAppKey
    ) {
        this.kakaoBaseUrl = kakaoBaseUrl;
        this.kakaoAppKey = kakaoAppKey;
    }

    /**
     * KakaoTalk FriendTalk Client Bean 생성
     */
    @Bean
    public NhnKakaoMessageClient kakaoFriendTalkClient() {
        RestClient restClient = RestClient.builder()
            .baseUrl(kakaoBaseUrl)
            .defaultUriVariables(java.util.Map.of("appkey", kakaoAppKey))
//            .requestInterceptor(logRequestInterceptor())
            .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(NhnKakaoMessageClient.class);
    }

    /**
     * RestClient에서 전송하는 url을 출력 확인하는 interceptor
     */
    private ClientHttpRequestInterceptor logRequestInterceptor() {
        return new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution
            ) throws IOException {
                log.info("Request URL: {}", request.getURI());
                log.info("Request Headers: {}", request.getHeaders());
                log.info("Request Body: {}", new String(body, StandardCharsets.UTF_8));
                return execution.execute(request, body);
            }
        };
    }
}
