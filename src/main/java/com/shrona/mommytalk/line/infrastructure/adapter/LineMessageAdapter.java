package com.shrona.mommytalk.line.infrastructure.adapter;

import com.shrona.mommytalk.line.infrastructure.sender.LineMessageSenderClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
public class LineMessageAdapter {

    private final String lineBaseUrl;

    public LineMessageAdapter(
        @Value("${line.base-url}") String lineBaseUrl) {
        this.lineBaseUrl = lineBaseUrl;
    }

    /**
     * 멀티 전송
     */
    @Bean
    public LineMessageSenderClient LineMessageMultiCastClient() {

        RestClient restClient = RestClient.builder()
            .baseUrl(lineBaseUrl + "/multicast")
//            .requestInterceptor(logRequestInterceptor())
            .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);

        // 어댑터를 기반으로 HTTP 서비스 프록시 팩토리를 빌드
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(LineMessageSenderClient.class);
    }

    /**
     * RestClient에서 전송하는 url을 출력 확인 하는 interceptor
     */
    private ClientHttpRequestInterceptor logRequestInterceptor() {
        return new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                ClientHttpRequestExecution execution) throws IOException {
                log.info("Request URL: {}", request.getURI()); // 출력 url 확인
                HttpHeaders headers = request.getHeaders();
                log.info(new String(body, StandardCharsets.UTF_8)); // Body 출력 확인
                return execution.execute(request, body);
            }
        };
    }
}
