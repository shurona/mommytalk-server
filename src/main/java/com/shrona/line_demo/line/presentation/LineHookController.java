package com.shrona.line_demo.line.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shrona.line_demo.line.presentation.dtos.WebHookRequestDto;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class LineHookController {

    private final Environment environment;

    @PostMapping
    public ResponseEntity<?> checkUrl(
        @RequestHeader("x-line-signature") String header,
        @RequestBody String requestBodyOrigin
    ) {

        System.out.println("?? : " + header);

        // TODO: 따로 메소드로 분리
        try {
            // header validation
            String channelSecret =
                this.environment.getProperty("line.secret-key");// Channel secret string

            SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] source = requestBodyOrigin.getBytes("UTF-8");
            String signature = Base64.encodeBase64String(mac.doFinal(source));

            System.out.println(signature + " : " + signature.equals(header));

            if (!signature.equals(header)) {
                return ResponseEntity.status(401).build();
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ObjectMapper mapper = new ObjectMapper();
            WebHookRequestDto requestBody = mapper.readValue(requestBodyOrigin,
                WebHookRequestDto.class);

            System.out.println("사이즈는 : " + requestBody.events().size());

            System.out.println(requestBodyOrigin); // 혹은 로그로 출력

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return ResponseEntity.ok().build();
    }


    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<String> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        // 예외 메시지 로그 남기기
        System.err.println("Missing header: " + ex.getHeaderName());

        // 클라이언트에 400 Bad Request와 메시지 전송
        return ResponseEntity
            .badRequest()
            .body("필수 헤더(" + ex.getHeaderName() + ")가 누락되었습니다.");
    }

}
