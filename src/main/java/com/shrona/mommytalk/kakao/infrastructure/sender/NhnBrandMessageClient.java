package com.shrona.mommytalk.kakao.infrastructure.sender;

import com.shrona.mommytalk.kakao.infrastructure.sender.dto.BrandMessageRequestDto;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.BrandMessageResponseDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * NHN Cloud Brand Message API 클라이언트
 * Endpoint: /brand-message/v1.0/appkeys/{appkey}/freestyle-messages
 */
@HttpExchange("/brand-message/v1.0/appkeys")
public interface NhnBrandMessageClient {

    /**
     * 브랜드 메시지 발송
     * POST /brand-message/v1.0/appkeys/{appkey}/freestyle-messages
     */
    @PostExchange("/{appkey}/freestyle-messages")
    BrandMessageResponseDto sendMessage(
        @RequestHeader("X-Secret-Key") String secretKey,
        @RequestBody BrandMessageRequestDto requestBody
    );
}
