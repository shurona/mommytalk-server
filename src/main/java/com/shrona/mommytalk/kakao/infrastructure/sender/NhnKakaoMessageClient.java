package com.shrona.mommytalk.kakao.infrastructure.sender;

import com.shrona.mommytalk.kakao.infrastructure.sender.dto.KakaoFriendTalkRequestDto;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.KakaoFriendTalkResponseDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/friendtalk/v2.4/appkeys")
public interface NhnKakaoMessageClient {

    /**
     * 친구톡 메시지 발송
     * POST /friendtalk/v2.4/appkeys/{appkey}/messages
     */
    @PostExchange("/{appkey}/messages")
    KakaoFriendTalkResponseDto sendMessage(
        @RequestHeader("X-Secret-Key") String secretKey,
        @RequestBody KakaoFriendTalkRequestDto requestBody
    );
}
