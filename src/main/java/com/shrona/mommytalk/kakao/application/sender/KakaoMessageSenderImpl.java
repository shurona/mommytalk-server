package com.shrona.mommytalk.kakao.application.sender;

import com.shrona.mommytalk.admin.application.AdminService;
import com.shrona.mommytalk.admin.presentation.form.TestUserForm;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.kakao.infrastructure.sender.NhnKakaoMessageClient;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.KakaoFriendTalkRequestDto;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.KakaoFriendTalkResponseDto;
import com.shrona.mommytalk.user.domain.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoMessageSenderImpl implements KakaoMessageSender {

    private static final int CHUNK_SIZE = 1000; // KakaoTalk FriendTalk은 최대 1000명
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm");

    private final NhnKakaoMessageClient nhnKakaoMessageClient;
    private final AdminService adminService;

    @Value("${kakao.secret-key}")
    private String kakaoSecretKey;

    @Override
    public void sendSingleMessage(Channel channel, User user, String content) {
        String senderKey = channel.getKakaoSenderKey();
        String recipientNo = user.getPhoneNumber().toString();

        try {
            KakaoFriendTalkRequestDto request = KakaoFriendTalkRequestDto.ofSingle(
                senderKey,
                recipientNo,
                content
            );

            KakaoFriendTalkResponseDto response = nhnKakaoMessageClient.sendMessage(
                kakaoSecretKey,
                request
            );

            logResponse(response);
        } catch (RestClientResponseException e) {
            log.error("[Kakao 단일 전송 에러] 수신자: {}, 에러: {}", recipientNo, e.getMessage());
            throw e;
        }

        sleepForRateLimit();
    }

    @Override
    public void sendMultiMessage(Channel channel, List<User> users, String content) {
        String senderKey = channel.getKakaoSenderKey();
        List<String> recipientNos = users.stream()
            .map(user -> user.getPhoneNumber().toString())
            .toList();

        if (recipientNos.isEmpty()) {
            log.warn("[Kakao 다중 전송] 수신자 목록이 비어있습니다.");
            return;
        }

        // 1000명씩 chunk로 나누어 전송
        for (int i = 0; i < recipientNos.size(); i += CHUNK_SIZE) {
            List<String> chunk = recipientNos.subList(
                i,
                Math.min(i + CHUNK_SIZE, recipientNos.size())
            );

            try {
                KakaoFriendTalkRequestDto request = KakaoFriendTalkRequestDto.ofMulti(
                    senderKey,
                    chunk,
                    content
                );

                KakaoFriendTalkResponseDto response = nhnKakaoMessageClient.sendMessage(
                    kakaoSecretKey,
                    request
                );

                logResponse(response);
            } catch (RestClientResponseException e) {
                log.error("[Kakao 다중 전송 에러] chunk 번호: {}, 에러: {}", i / CHUNK_SIZE, e.getMessage());
                throw e;
            }

            sleepForRateLimit();
        }
    }

    @Override
    public void sendScheduledMessage(
        Channel channel,
        List<User> users,
        String content,
        LocalDateTime scheduledTime
    ) {
        String senderKey = channel.getKakaoSenderKey();
        List<String> recipientNos = users.stream()
            .map(user -> user.getPhoneNumber().toString())
            .toList();

        if (recipientNos.isEmpty()) {
            log.warn("[Kakao 예약 전송] 수신자 목록이 비어있습니다.");
            return;
        }

        String requestDate = scheduledTime.format(DATE_FORMATTER);

        // 1000명씩 chunk로 나누어 전송
        for (int i = 0; i < recipientNos.size(); i += CHUNK_SIZE) {
            List<String> chunk = recipientNos.subList(
                i,
                Math.min(i + CHUNK_SIZE, recipientNos.size())
            );

            try {
                KakaoFriendTalkRequestDto request = KakaoFriendTalkRequestDto.ofScheduled(
                    senderKey,
                    chunk,
                    content,
                    requestDate
                );

                KakaoFriendTalkResponseDto response = nhnKakaoMessageClient.sendMessage(
                    kakaoSecretKey,
                    request
                );

                log.info("[Kakao 예약 전송] 예약 시간: {}, requestId: {}", requestDate,
                    response.requestId());
                logResponse(response);
            } catch (RestClientResponseException e) {
                log.error("[Kakao 예약 전송 에러] chunk 번호: {}, 에러: {}", i / CHUNK_SIZE, e.getMessage());
                throw e;
            }

            sleepForRateLimit();
        }
    }

    @Override
    public boolean sendTestMessage(Channel channel, String content) {
        // 테스트 유저 목록 조회
        List<String> testPhoneNumbers = adminService.findAllTestUser(channel)
            .stream()
            .map(TestUserForm::phoneNumber)
            .filter(phone -> phone != null && !phone.isBlank())
            .toList();

        log.info("[Kakao 테스트 메시지] 발송 전화번호 목록: {}", testPhoneNumbers);

        if (testPhoneNumbers.isEmpty()) {
            log.warn("[Kakao 테스트 메시지] 테스트 유저가 없습니다.");
            return true;
        }

        String senderKey = channel.getKakaoSenderKey();

        try {
            KakaoFriendTalkRequestDto request = KakaoFriendTalkRequestDto.ofMulti(
                senderKey,
                testPhoneNumbers,
                content
            );

            KakaoFriendTalkResponseDto response = nhnKakaoMessageClient.sendMessage(
                kakaoSecretKey,
                request
            );

            logResponse(response);
            return Boolean.TRUE.equals(response.isSuccessful());
        } catch (Exception e) {
            log.error("[Kakao 테스트 메시지 에러] 에러: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 응답 로깅
     */
    private void logResponse(KakaoFriendTalkResponseDto response) {
        log.info("[Kakao 발송 응답] requestId: {}, 성공: {}, 성공 수: {}, 실패 수: {}",
            response.requestId(),
            response.isSuccessful(),
            response.successCount(),
            response.failCount()
        );

        if (response.failCount() != null && response.failCount() > 0) {
            log.warn("[Kakao 발송 실패 상세] {}", response.recipientList());
        }
    }

    /**
     * Rate Limit 방지를 위한 딜레이
     */
    private void sleepForRateLimit() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Rate limit sleep interrupted", e);
        }
    }
}
