package com.shrona.mommytalk.kakao.infrastructure.adapter;

import com.shrona.mommytalk.kakao.infrastructure.sender.KakaoAuthClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
public class KakaoAuthAdapter {

    private static final String KAKAO_API_BASE_URL = "https://kapi.kakao.com";
    private static final String KAKAO_AUTH_BASE_URL = "https://kauth.kakao.com";

    /**
     * Kakao Auth용 RestClient (토큰 교환용 - form data)
     */
    @Bean
    public RestClient kakaoAuthRestClient() {
        return RestClient.builder()
            .baseUrl(KAKAO_AUTH_BASE_URL)
            .build();
    }

    /**
     * Kakao API용 RestClient (사용자 정보 조회용)
     */
    @Bean
    public KakaoAuthClient kakaoAuthClient() {

        RestClient restClient = RestClient.builder()
            .baseUrl(KAKAO_API_BASE_URL)
            .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        // 어댑터를 기반으로 HTTP 서비스 프록시 팩토리를 빌드
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(KakaoAuthClient.class);
    }
}
