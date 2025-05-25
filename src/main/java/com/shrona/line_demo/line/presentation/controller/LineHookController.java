package com.shrona.line_demo.line.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shrona.line_demo.line.application.LineService;
import com.shrona.line_demo.line.presentation.dtos.LineEvent;
import com.shrona.line_demo.line.presentation.dtos.WebHookRequestDto;
import com.shrona.line_demo.line.presentation.validation.LineValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
public class LineHookController {

    // service
    private final LineService lineService;
    private final LineValidation lineValidation;

    //
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<String> homeEntity() {
        return ResponseEntity.ok().body("안녕하세요~");
    }

    @PostMapping
    public ResponseEntity<String> rcvLineHook(
        @RequestHeader("x-line-signature") String header,
        @RequestBody String requestBodyOrigin
    ) {

        // 잘못된 접근은 제한 처리
        // hook처리여서 ok 처리
        if (!lineValidation.checkLineSignature(requestBodyOrigin, header)) {
            return ResponseEntity.ok().build();
        }

        try {
            // Body String을 Body 객체로 변환
            WebHookRequestDto requestBody = objectMapper.readValue(requestBodyOrigin,
                WebHookRequestDto.class);

            for (LineEvent event : requestBody.events()) {
                switch (event.type()) {
                    case "message":
                        // text일 시 저장
                        if (event.message().type().equals("text")) {
                            lineService.saveLineMessage(event.source().userId(),
                                event.message().text());
                        }
                        break;

                    case "follow":
                        lineService.followLineUserByLineId(event.source().userId());
                        break;

                    case "unfollow":
                        lineService.unfollowLineUserByLineId(event.source().userId());
                        break;

                    default:
                        log.info("unsupported event type : " + requestBodyOrigin.toString());
                        break;
                }

            }


        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return ResponseEntity.ok().build();
    }


    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<String> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        // 예외 메시지 로그 남기기
        log.error("Missing header: " + ex.getHeaderName());

        // 클라이언트에 400 Bad Request와 메시지 전송
        return ResponseEntity
            .badRequest().build();
    }

}
