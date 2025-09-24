package com.shrona.mommytalk.linehook.validation;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.line.infrastructure.repository.jpa.ChannelJpaRepository;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class LineValidation {

    private final ObjectMapper objectMapper;

    private final ChannelJpaRepository channelRepository;

    public boolean checkLineSignature(String body, String header, Long channelId) {

        try {
            Optional<Channel> channelInfo = channelRepository.findById(channelId);

            if (channelInfo.isEmpty()) {
                return false;
            }

            // channel secret key
            String channelSecret =
                base64ToUtf8(channelInfo.get().getChannelId());// Channel secret string

            // 들어온 body의 키 정렬
            objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            String canonicalJson = objectMapper.writeValueAsString(objectMapper.readTree(body));

            SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] source = canonicalJson.getBytes(UTF_8);
            String signature = Base64.getEncoder().encodeToString(mac.doFinal(source));

            log.info(channelId + " : " + signature + " : " + signature.equals(header));

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


    private String base64ToUtf8(String accessToken) {
        byte[] decodedBytes = Base64.getDecoder().decode(accessToken);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

}
