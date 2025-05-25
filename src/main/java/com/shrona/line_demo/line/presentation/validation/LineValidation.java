package com.shrona.line_demo.line.presentation.validation;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class LineValidation {

    private final Environment environment;
    private final ObjectMapper objectMapper;

    public boolean checkLineSignature(String body, String header) {

        try {
            String channelSecret =
                this.environment.getProperty("line.secret-key");// Channel secret string

            // 들어온 body의 키 정렬
            objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            String canonicalJson = objectMapper.writeValueAsString(objectMapper.readTree(body));

            SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] source = canonicalJson.getBytes(UTF_8);
            String signature = Base64.getEncoder().encodeToString(mac.doFinal(source));

            log.info(signature + " : " + signature.equals(header));

            if (!signature.equals(header)) {
                log.error("잘못된 접근 입니다.");
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }

        return true;
    }


}
