package com.shrona.mommytalk.kakao.infrastructure.sender;

import com.shrona.mommytalk.kakao.infrastructure.sender.dto.AlimtalkMessageRequestDto;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.AlimtalkMessageResponseDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * NHN Cloud Alimtalk API 클라이언트
 * Endpoint: /alimtalk/v2.3/appkeys/{appkey}/messages
 */
@HttpExchange("/alimtalk/v2.3/appkeys")
public interface NhnAlimtalkMessageClient {

    /**
     * 알림톡 발송 (치환 발송)
     * POST /alimtalk/v2.3/appkeys/{appkey}/messages
     */
    @PostExchange("/{appkey}/messages")
    AlimtalkMessageResponseDto sendMessage(
        @RequestHeader("X-Secret-Key") String secretKey,
        @RequestBody AlimtalkMessageRequestDto requestBody
    );
}
