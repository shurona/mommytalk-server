package com.shrona.mommytalk.common.filter;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Base64;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class LineHookFilter extends OncePerRequestFilter {


    private final Environment environment;

    @Override
    protected void doFilterInternal(
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse,
        FilterChain filterChain) throws ServletException, IOException {

        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(
            servletRequest);

        try {
            String header = servletRequest.getHeader("x-line-signature");

            // Body 읽기
            String body = new BufferedReader(wrappedRequest.getReader())
                .lines()
                .collect(Collectors.joining());

            // body를 정렬
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true); // 키 순서 정렬
            String canonicalJson = objectMapper.writeValueAsString(objectMapper.readTree(body));

            String channelSecret =
                this.environment.getProperty("line.secret-key");// Channel secret string

            SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] source = canonicalJson.getBytes(UTF_8);
            String signature = Base64.getEncoder().encodeToString(mac.doFinal(source));

//
            if (!signature.equals(header)) {
                log.error("잘못된 접근 입니다.");
                servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        filterChain.doFilter(wrappedRequest, servletResponse);

    }
}
