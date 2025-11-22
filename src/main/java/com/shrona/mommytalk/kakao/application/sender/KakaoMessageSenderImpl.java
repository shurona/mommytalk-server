package com.shrona.mommytalk.kakao.application.sender;

import static com.shrona.mommytalk.message.domain.type.ReservationStatus.COMPLETE;
import static com.shrona.mommytalk.message.domain.type.ReservationStatus.FAIL;

import com.shrona.mommytalk.admin.application.AdminService;
import com.shrona.mommytalk.admin.presentation.form.TestUserServiceDto;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.elevenlabs.domain.ElevenLabsMedia;
import com.shrona.mommytalk.entitlement.domain.EntitlementType;
import com.shrona.mommytalk.kakao.infrastructure.sender.NhnBrandMessageClient;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.BrandMessageRequestDto;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.BrandMessageRequestDto.ButtonDto;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.BrandMessageResponseDto;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageContentQueryRepository;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageLogDetailQueryRepository;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageQueryRepository;
import com.shrona.mommytalk.user.domain.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoMessageSenderImpl implements KakaoMessageSender {

    // 메시지 전송 status
    private static final int SEND_FAIL = 0;
    private static final int SEND_SUCCESS = 1;
    private static final int CHUNK_SIZE = 1000; // 브랜드 메시지 API 최대 수신자 수
    private static final int RATE_LIMIT_DELAY_MS = 50; // 개별 호출 시 rate limit 방지 delay (ms)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm");

    // restClient
    private final NhnBrandMessageClient nhnBrandMessageClient;
    private final AdminService adminService;

    // repository
    private final MessageQueryRepository messageRepository;
    private final MessageLogDetailQueryRepository messageLogDetailQueryRepository;
    private final MessageContentQueryRepository messageContentQueryRepository;


    @Value("${kakao.secret-key}")
    private String kakaoSecretKey;

    @Value("${front.base-url}")
    private String frontBaseUrl;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendKakaoMessageByReservationByMessageIds(
        List<Long> messageIds, List<ReservationStatus> statusList) {

        List<MessageLog> kakaoMessageByIds = messageRepository.findMessageByIds(messageIds);

        for (MessageLog messageLog : kakaoMessageByIds) {

            // messageLogId가 동일하고 예약 상태인 messageLogDetail 목록을 갖고 온다.
            List<MessageLogDetail> mldList = messageLogDetailQueryRepository
                .findMldListByStatusWithKakao(messageLog.getId(), statusList);

            // MessageContent.id를 기준으로 MessageLogDetail 목록 그룹핑 (레벨별 그룹화 유지)
            Map<Long, List<MessageLogDetail>> mldListByContentId = mldList.stream()
                .collect(Collectors.groupingBy(mld -> mld.getMessageContent().getId()));

            for (Map.Entry<Long, List<MessageLogDetail>> entry : mldListByContentId.entrySet()) {
                List<MessageLogDetail> contentMldList = entry.getValue();

                // 메시지 로그의 상품 정보가 있는 경우 그에 맞춰서 로직을 수행되게 한다.
                switch (messageLog.getEntitlement().getType()) {
                    // 일반 유저
                    case EntitlementType.MOMMYTALK -> {
                        // 메시지 전송 (내부에서 상태 업데이트 완료)
                        sendMessageToKakao(
                            messageLog.getChannel(),
                            contentMldList,
                            getReserveTimeIfPassed(messageLog)
                        );
                    }
                    // 마미 보카
                    case EntitlementType.MOMMYVOCA -> {
                        // 메시지 전송 (내부에서 상태 업데이트 완료)
                        sendMessageToKakaoWithDiary(
                            messageLog.getChannel(),
                            contentMldList,
                            getReserveTimeIfPassed(messageLog)
                        );
                    }
                }
            }
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
            .map(user -> user.getPhoneNumber().getPhoneNumber())
            .toList();

        if (recipientNos.isEmpty()) {
            log.warn("[Kakao 예약 전송] 수신자 목록이 비어있습니다.");
            return;
        }

        String requestDate = scheduledTime.format(DATE_FORMATTER);

        // 1000명씩 chunk로 나누어 전송 (동일 content)
        for (int i = 0; i < recipientNos.size(); i += CHUNK_SIZE) {
            List<String> chunk = recipientNos.subList(
                i,
                Math.min(i + CHUNK_SIZE, recipientNos.size())
            );

            try {
                BrandMessageRequestDto request = BrandMessageRequestDto.ofMulti(
                    senderKey,
                    chunk,
                    content,
                    null,  // 버튼 없음
                    requestDate
                );

                BrandMessageResponseDto response = nhnBrandMessageClient.sendMessage(
                    kakaoSecretKey,
                    request
                );

                log.info("[Kakao 예약 전송] 예약 시간: {}", requestDate);
                logResponse(response);
            } catch (RestClientResponseException e) {
                log.error("[Kakao 예약 전송 에러] chunk 번호: {}, 에러: {}", i / CHUNK_SIZE, e.getMessage());
                throw e;
            }

            sleepForRateLimit();
        }
    }

    @Override
    public boolean sendTestMessage(Channel channel, Long messageContentId) {
        // 테스트 유저 목록 조회
        List<String> testPhoneNumbers = adminService.findAllTestUser(channel)
            .stream()
            .map(TestUserServiceDto::phoneNumber)
            .filter(phone -> phone != null && !phone.isBlank())
            .toList();

        MessageContent content = messageContentQueryRepository.findById(messageContentId);

        log.info("[Kakao 테스트 메시지] 발송 전화번호 목록: {}", testPhoneNumbers);

        if (testPhoneNumbers.isEmpty()) {
            log.warn("[Kakao 테스트 메시지] 테스트 유저가 없습니다.");
            return true;
        }

        String senderKey = channel.getKakaoSenderKey();

        try {
            // URL 추출
            String voiceUrl = Optional.ofNullable(content.getHeaderOneLink())
                .map(ElevenLabsMedia::getFileUrl)
                .orElse(null);
            String mommyVocaUrl = content.getMommyVoca();

            // mommyVocaUrl 유무로 MOMMYTALK/MOMMYVOCA 판단
            List<ButtonDto> buttons;
            if (mommyVocaUrl != null && !mommyVocaUrl.trim().isEmpty()) {
                // MOMMYVOCA: 세로 배치 (발음듣기, 마미보카, 나만의 문장 만들기)
                buttons = createButtonsForMommyVoca(voiceUrl, mommyVocaUrl, 152L);
            } else {
                // MOMMYTALK: 가로 배치 (발음듣기, 나만의 문장 만들기)
                buttons = createButtonsForMommyTalk(voiceUrl, 152L);
            }

            // 브랜드 메시지는 개인화 미지원이므로 ofMulti 사용 (모든 테스트 유저에게 동일 메시지)
            BrandMessageRequestDto requestBody = BrandMessageRequestDto.ofMulti(
                senderKey,
                testPhoneNumbers,
                content.getContent(),
                buttons,
                null  // 즉시 전송
            );

            BrandMessageResponseDto response = nhnBrandMessageClient.sendMessage(
                kakaoSecretKey,
                requestBody
            );

            boolean isSuccessful = logResponse(response);
            return isSuccessful;
        } catch (Exception e) {
            log.error("[Kakao 테스트 메시지 에러] 에러: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Rate Limit 방지를 위한 딜레이
     * 브랜드 메시지 API는 개별 호출이 많아지므로 delay를 늘림
     */
    private void sleepForRateLimit() {
        try {
            Thread.sleep(RATE_LIMIT_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Rate limit sleep interrupted", e);
        }
    }

    /**
     * MOMMYTALK 메시지를 카카오에 전달한다. (개인화 지원)
     * {아이이름} 템플릿 변수 치환 + 가로 배치 버튼 (발음듣기, 나만의 문장 만들기)
     * <p>
     * 브랜드 메시지 API는 개인화를 지원하지 않으므로 수신자별로 개별 API 호출
     */
    private int sendMessageToKakao(
        Channel channel,
        List<MessageLogDetail> mldList,
        LocalDateTime reserveTime) {

        String senderKey = channel.getKakaoSenderKey();
        if (mldList.isEmpty() || senderKey == null || senderKey.isBlank()) {
            return SEND_SUCCESS;
        }

        // 첫 번째 MessageContent에서 발음듣기 URL 추출
        MessageContent firstContent = mldList.get(0).getMessageContent();
        String voiceUrl = Optional.ofNullable(firstContent.getHeaderOneLink())
            .map(ElevenLabsMedia::getFileUrl)
            .orElse(null);

        String requestDate = reserveTime.format(DATE_FORMATTER);

        // 성공/실패 ID 수집용 리스트
        List<Long> successIds = new ArrayList<>();
        List<Long> failIds = new ArrayList<>();

        // 브랜드 메시지는 개인화 미지원 -> 수신자별로 개별 API 호출
        for (MessageLogDetail mld : mldList) {
            User user = mld.getUser();
            MessageContent content = mld.getMessageContent();

            // {아이이름} 템플릿 변수 치환
            String childName = user.getChildName() != null ? user.getChildName() : "아이는";
            String personalizedContent = content.getContent().replace("{아이이름}", childName);

            // MOMMYTALK 버튼 생성 (가로 배치)
            List<ButtonDto> buttons = createButtonsForMommyTalk(voiceUrl, mld.getId());

            try {
                BrandMessageRequestDto request = BrandMessageRequestDto.ofSingle(
                    senderKey,
                    user.getPhoneNumber().getPhoneNumber(),
                    personalizedContent,
                    buttons,
                    requestDate
                );

                BrandMessageResponseDto response = nhnBrandMessageClient.sendMessage(
                    kakaoSecretKey,
                    request
                );

                if (logResponse(response)) {
                    successIds.add(mld.getId());
                } else {
                    failIds.add(mld.getId());
                }
            } catch (RestClientResponseException e) {
                log.error("[MOMMYTALK 개별 전송 에러] 수신자: {}, 에러: {}",
                    user.getPhoneNumber().getPhoneNumber(), e.getMessage());
                failIds.add(mld.getId());
            }

            // Rate limit 방지
            sleepForRateLimit();
        }

        // Batch 업데이트: 성공/실패 ID 목록으로 상태 변경
        if (!successIds.isEmpty()) {
            messageLogDetailQueryRepository.updateStatusByIds(successIds, COMPLETE);
        }
        if (!failIds.isEmpty()) {
            messageLogDetailQueryRepository.updateStatusByIds(failIds, FAIL);
        }

        log.info("[MOMMYTALK 개인화 전송 완료] 성공: {}, 실패: {}", successIds.size(), failIds.size());
        return failIds.isEmpty() ? SEND_SUCCESS : SEND_FAIL;
    }

    /**
     * MOMMYVOCA 메시지를 카카오에 전달한다. (개인화 지원)
     * {아이이름} 템플릿 변수 치환 + 세로 배치 버튼 (발음듣기, 마미보카, 나만의 문장 만들기)
     * <p>
     * 브랜드 메시지 API는 개인화를 지원하지 않으므로 수신자별로 개별 API 호출
     */
    private int sendMessageToKakaoWithDiary(
        Channel channel,
        List<MessageLogDetail> mldList,
        LocalDateTime reserveTime) {

        String senderKey = channel.getKakaoSenderKey();
        if (mldList.isEmpty() || senderKey == null || senderKey.isBlank()) {
            return SEND_SUCCESS;
        }

        // 첫 번째 MessageContent에서 URL 정보 추출
        MessageContent firstContent = mldList.get(0).getMessageContent();
        String voiceUrl = Optional.ofNullable(firstContent.getHeaderOneLink())
            .map(ElevenLabsMedia::getFileUrl)
            .orElse(null);
        String mommyVocaUrl = firstContent.getMommyVoca();

        String requestDate = reserveTime.format(DATE_FORMATTER);

        // 성공/실패 ID 수집용 리스트
        List<Long> successIds = new ArrayList<>();
        List<Long> failIds = new ArrayList<>();

        // 브랜드 메시지는 개인화 미지원 -> 수신자별로 개별 API 호출
        for (MessageLogDetail mld : mldList) {
            User user = mld.getUser();
            MessageContent content = mld.getMessageContent();

            // {아이이름} 템플릿 변수 치환
            String childName = user.getChildName() != null ? user.getChildName() : "아이이름";
            String personalizedContent = content.getContent().replace("{아이이름}", childName);

            // MOMMYVOCA 버튼 생성 (세로 배치)
            List<ButtonDto> buttons = createButtonsForMommyVoca(voiceUrl, mommyVocaUrl,
                mld.getId());

            try {
                BrandMessageRequestDto request = BrandMessageRequestDto.ofSingle(
                    senderKey,
                    user.getPhoneNumber().getPhoneNumber(),
                    personalizedContent,
                    buttons,
                    requestDate
                );

                BrandMessageResponseDto response = nhnBrandMessageClient.sendMessage(
                    kakaoSecretKey,
                    request
                );

                if (logResponse(response)) {
                    successIds.add(mld.getId());
                } else {
                    failIds.add(mld.getId());
                }
            } catch (RestClientResponseException e) {
                log.error("[MOMMYVOCA 개별 전송 에러] 수신자: {}, 에러: {}",
                    user.getPhoneNumber().getPhoneNumber(), e.getMessage());
                failIds.add(mld.getId());
            }

            // Rate limit 방지
            sleepForRateLimit();
        }

        // Batch 업데이트: 성공/실패 ID 목록으로 상태 변경
        if (!successIds.isEmpty()) {
            messageLogDetailQueryRepository.updateStatusByIds(successIds, COMPLETE);
        }
        if (!failIds.isEmpty()) {
            messageLogDetailQueryRepository.updateStatusByIds(failIds, FAIL);
        }

        log.info("[MOMMYVOCA 개인화 전송 완료] 성공: {}, 실패: {}", successIds.size(), failIds.size());
        return failIds.isEmpty() ? SEND_SUCCESS : SEND_FAIL;
    }

    /**
     * 응답 로깅 (Brand Message API)
     */
    private Boolean logResponse(BrandMessageResponseDto response) {
        boolean isSuccessful = response.header() != null &&
            Boolean.TRUE.equals(response.header().isSuccessful());

        int successCount = 0;
        int failCount = 0;

        if (response.message() != null && response.message().sendResults() != null) {
            for (BrandMessageResponseDto.SendResult result : response.message().sendResults()) {
                if (result.resultCode() != null && result.resultCode() == 0) {
                    successCount++;
                } else {
                    failCount++;
                }
            }
        }

        log.info("[Kakao 발송 응답] 성공: {}, 성공 수: {}, 실패 수: {}",
            isSuccessful,
            successCount,
            failCount
        );

        if (!isSuccessful) {
            log.warn("[Kakao 발송 실패 상세] {}", response);
        }

        return isSuccessful;
    }


    /**
     * MOMMYTALK용 버튼 생성 (가로 배치)
     * 1. 발음듣기 🔈
     * 2. ➕ 나만의 문장 만들기
     */
    private List<ButtonDto> createButtonsForMommyTalk(String voiceUrl, Long messageLogDetailId) {
        List<ButtonDto> buttons = new java.util.ArrayList<>();

        // 1. 발음듣기 버튼
        String voiceLink = frontBaseUrl + "mommytalk365/" + messageLogDetailId;
        if (voiceUrl != null && !voiceUrl.trim().isEmpty()) {
            buttons.add(new ButtonDto(
                "WL",
                "발음듣기 🔈",
                voiceLink,
                voiceLink,
                null,
                null
            ));
        }

        // 2. 나만의 문장 만들기 버튼
        String customSentenceUrl = frontBaseUrl + "dashboard";
        buttons.add(new ButtonDto(
            "WL",
            "➕ 나만의 문장 만들기",
            customSentenceUrl,
            customSentenceUrl,
            null,
            null
        ));

        return buttons;
    }

    /**
     * MOMMYVOCA용 버튼 생성 (세로 배치)
     * 1. 발음듣기 🔈
     * 2. 마미보카 💌
     * 3. ➕ 나만의 문장 만들기
     */
    private List<ButtonDto> createButtonsForMommyVoca(
        String voiceUrl, String mommyVocaUrl, Long messageLogDetailId) {
        List<ButtonDto> buttons = new java.util.ArrayList<>();

        // 1. 발음듣기 버튼
        String voiceLink = frontBaseUrl + "mommytalk365/" + messageLogDetailId;
        if (voiceUrl != null && !voiceUrl.trim().isEmpty()) {
            buttons.add(new ButtonDto(
                "WL",
                "발음듣기 🔈",
                voiceLink,
                voiceLink,
                null,
                null
            ));
        }

        // 2. 마미보카 버튼
        if (mommyVocaUrl != null && !mommyVocaUrl.trim().isEmpty()) {
            buttons.add(new ButtonDto(
                "WL",
                "마미보카 💌",
                mommyVocaUrl.trim(),
                mommyVocaUrl.trim(),
                null,
                null
            ));
        }

        // 3. 나만의 문장 만들기 버튼
        String customSentenceUrl = frontBaseUrl + "dashboard";
        buttons.add(new ButtonDto(
            "WL",
            "➕ 나만의 문장 만들기",
            customSentenceUrl,
            customSentenceUrl,
            null,
            null
        ));

        return buttons;
    }

    /**
     * 현재보다 과거이면 1분 뒤로 전송한다.
     */
    private LocalDateTime getReserveTimeIfPassed(MessageLog messageLog) {
        return LocalDateTime.now().isAfter(messageLog.getReserveTime())
            ? LocalDateTime.now().plusHours(9).plusMinutes(1)// 약간 뒤의 시간으로 예약한다.
            : messageLog.getReserveTime().plusHours(9);
    }

}
