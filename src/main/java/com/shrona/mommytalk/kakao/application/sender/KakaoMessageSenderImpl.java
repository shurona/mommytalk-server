package com.shrona.mommytalk.kakao.application.sender;

import static com.shrona.mommytalk.message.domain.type.ReservationStatus.COMPLETE;
import static com.shrona.mommytalk.message.domain.type.ReservationStatus.FAIL;

import com.shrona.mommytalk.admin.application.AdminService;
import com.shrona.mommytalk.admin.presentation.form.TestUserServiceDto;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.elevenlabs.domain.ElevenLabsMedia;
import com.shrona.mommytalk.entitlement.domain.EntitlementType;
import com.shrona.mommytalk.kakao.infrastructure.sender.NhnKakaoMessageClient;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.KakaoFriendTalkRequestDto;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.KakaoFriendTalkRequestDto.ButtonDto;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.KakaoFriendTalkResponseDto;
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
    private static final int CHUNK_SIZE = 1000; // KakaoTalk FriendTalk은 최대 1000명
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm");

    // restClient
    private final NhnKakaoMessageClient nhnKakaoMessageClient;
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
                Long messageContentId = entry.getKey();
                List<MessageLogDetail> contentMldList = entry.getValue();

                // 메시지 로그의 상품 정보가 있는 경우 그에 맞춰서 로직을 수행되게 한다.
                int sendStatus;
                switch (messageLog.getEntitlement().getType()) {
                    // 일반 유저
                    case EntitlementType.MOMMYTALK -> {
                        // 메시지 전송
                        sendStatus = sendMessageToKakao(
                            messageLog.getChannel(),
                            contentMldList,
                            getReserveTimeIfPassed(messageLog)
                        );
                    }
                    // 마미 보카
                    case EntitlementType.MOMMYVOCA -> {
                        sendStatus = sendMessageToKakaoWithDiary(
                            messageLog.getChannel(),
                            contentMldList,
                            getReserveTimeIfPassed(messageLog)
                        );
                    }
                    default -> {
                        sendStatus = -1;
                    }
                }

                // 전송 성공 시 메시지 상태 변경 및 sender time 설정
                if (sendStatus == SEND_SUCCESS) {
                    // smtId인 MessageLogDetailInfo를 업데이트 해준다.
                    messageLogDetailQueryRepository.updateStatusByContentId(
                        messageContentId, messageLog.getId(), COMPLETE);
                } else if (sendStatus == SEND_FAIL) {
                    messageLogDetailQueryRepository.updateStatusByContentId(
                        messageContentId, messageLog.getId(), FAIL);
                }
            }
        }
    }

    @Override
    public void sendSingleMessage(Channel channel, User user, String content) {
        String senderKey = channel.getKakaoSenderKey();
        String recipientNo = user.getPhoneNumber().getPhoneNumber();

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

            if (!logResponse(response)) {
                throw new RuntimeException();
            }
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
            .map(user -> user.getPhoneNumber().getPhoneNumber())
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
                    content,
                    null
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
            .map(user -> user.getPhoneNumber().getPhoneNumber())
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
                buttons = createButtonsForMommyVoca(voiceUrl, mommyVocaUrl, content.getId());
            } else {
                // MOMMYTALK: 가로 배치 (발음듣기, 나만의 문장 만들기)
                buttons = createButtonsForMommyTalk(voiceUrl, content.getId());
            }

            // RecipientDto 목록 생성 (테스트는 개인화 없음)
            List<KakaoFriendTalkRequestDto.RecipientDto> recipients = testPhoneNumbers.stream()
                .map(phone -> new KakaoFriendTalkRequestDto.RecipientDto(
                    phone,
                    content.getContent(),
                    buttons,
                    null,
                    null
                ))
                .toList();

            KakaoFriendTalkRequestDto requestBody = KakaoFriendTalkRequestDto.ofPersonalized(
                senderKey,
                recipients,
                null
            );

            KakaoFriendTalkResponseDto response = nhnKakaoMessageClient.sendMessage(
                kakaoSecretKey,
                requestBody
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
    private Boolean logResponse(KakaoFriendTalkResponseDto response) {
        log.info("[Kakao 발송 응답] requestId: {}, 성공: {}, 성공 수: {}, 실패 수: {}",
            response.requestId(),
            response.isSuccessful(),
            response.successCount(),
            response.failCount()
        );

        if (!response.isSuccessful()) {
            log.warn("[Kakao 발송 실패 상세] {}", response);

        }

        return response.isSuccessful();
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

    /**
     * MOMMYTALK 메시지를 카카오에 전달한다. (개인화 지원)
     * {아이이름} 템플릿 변수 치환 + 가로 배치 버튼 (발음듣기, 나만의 문장 만들기)
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

        // MOMMYTALK용 개인화된 RecipientDto 목록 생성
        List<KakaoFriendTalkRequestDto.RecipientDto> allRecipients =
            buildPersonalizedRecipientsForMommyTalk(mldList, voiceUrl);

        // 카카오 메시지 전송 (1000명씩 chunk)
        for (int i = 0; i < allRecipients.size(); i += CHUNK_SIZE) {
            List<KakaoFriendTalkRequestDto.RecipientDto> chunk = allRecipients.subList(i,
                Math.min(i + CHUNK_SIZE, allRecipients.size()));
            try {
                KakaoFriendTalkRequestDto requestBody = KakaoFriendTalkRequestDto.ofPersonalized(
                    senderKey,
                    chunk,
                    reserveTime.format(DATE_FORMATTER)
                );

                KakaoFriendTalkResponseDto response = nhnKakaoMessageClient.sendMessage(
                    kakaoSecretKey,
                    requestBody
                );

                if (!logResponse(response)) {
                    return SEND_FAIL;
                }
            } catch (RestClientResponseException e) {
                log.error("[MOMMYTALK 전송 중 에러 발생] {} 번째 chunk에서 에러 발생\n에러 원인 {}",
                    i / CHUNK_SIZE, e.getMessage());
                return SEND_FAIL;
            }

            sleepForRateLimit();
        }

        return SEND_SUCCESS;
    }

    /**
     * MOMMYVOCA 메시지를 카카오에 전달한다. (개인화 지원)
     * {아이이름} 템플릿 변수 치환 + 세로 배치 버튼 (발음듣기, 마미보카, 나만의 문장 만들기)
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

        // MOMMYVOCA용 개인화된 RecipientDto 목록 생성
        List<KakaoFriendTalkRequestDto.RecipientDto> allRecipients =
            buildPersonalizedRecipientsForMommyVoca(mldList, voiceUrl, mommyVocaUrl);

        // 카카오 메시지 전송 (1000명씩 chunk)
        for (int i = 0; i < allRecipients.size(); i += CHUNK_SIZE) {
            List<KakaoFriendTalkRequestDto.RecipientDto> chunk = allRecipients.subList(i,
                Math.min(i + CHUNK_SIZE, allRecipients.size()));
            try {
                KakaoFriendTalkRequestDto requestBody = KakaoFriendTalkRequestDto.ofPersonalized(
                    senderKey,
                    chunk,
                    reserveTime.format(DATE_FORMATTER)
                );

                KakaoFriendTalkResponseDto response = nhnKakaoMessageClient.sendMessage(
                    kakaoSecretKey,
                    requestBody
                );

                if (!logResponse(response)) {
                    return SEND_FAIL;
                }
            } catch (RestClientResponseException e) {
                log.error("[MOMMYVOCA 전송 중 에러 발생] {} 번째 chunk에서 에러 발생\n에러 원인 {}",
                    i / CHUNK_SIZE, e.getMessage());
                return SEND_FAIL;
            }

            sleepForRateLimit();
        }

        return SEND_SUCCESS;
    }

    /**
     * MOMMYTALK용 버튼 생성 (가로 배치)
     * 1. 발음듣기 🔈
     * 2. ➕ 나만의 문장 만들기
     */
    private List<ButtonDto> createButtonsForMommyTalk(String voiceUrl, Long messageContentId) {
        List<ButtonDto> buttons = new java.util.ArrayList<>();

        // 1. 발음듣기 버튼
        if (voiceUrl != null && !voiceUrl.trim().isEmpty()) {
            buttons.add(new ButtonDto(
                "1",
                "WL",
                "발음듣기 🔈",
                voiceUrl.trim(),
                voiceUrl.trim(),
                null,
                null
            ));
        }

        // 2. 나만의 문장 만들기 버튼
        String customSentenceUrl = frontBaseUrl + "/mommytalk365/" + messageContentId;
        buttons.add(new ButtonDto(
            "2",
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
    private List<ButtonDto> createButtonsForMommyVoca(String voiceUrl, String mommyVocaUrl, Long messageContentId) {
        List<ButtonDto> buttons = new java.util.ArrayList<>();

        // 1. 발음듣기 버튼
        if (voiceUrl != null && !voiceUrl.trim().isEmpty()) {
            buttons.add(new ButtonDto(
                "1",
                "WL",
                "발음듣기 🔈",
                voiceUrl.trim(),
                voiceUrl.trim(),
                null,
                null
            ));
        }

        // 2. 마미보카 버튼
        if (mommyVocaUrl != null && !mommyVocaUrl.trim().isEmpty()) {
            buttons.add(new ButtonDto(
                "2",
                "WL",
                "마미보카 💌",
                mommyVocaUrl.trim(),
                mommyVocaUrl.trim(),
                null,
                null
            ));
        }

        // 3. 나만의 문장 만들기 버튼
        String customSentenceUrl = frontBaseUrl + "/mommytalk365/" + messageContentId;
        buttons.add(new ButtonDto(
            "3",
            "WL",
            "➕ 나만의 문장 만들기",
            customSentenceUrl,
            customSentenceUrl,
            null,
            null
        ));

        return buttons;
    }

    private LocalDateTime getReserveTimeIfPassed(MessageLog messageLog) {
        return LocalDateTime.now().isAfter(messageLog.getReserveTime())
            ? LocalDateTime.now().plusHours(9).plusMinutes(1)// 약간 뒤의 시간으로 예약한다.
            : messageLog.getReserveTime().plusHours(9);
    }

    /**
     * MOMMYTALK용 개인화된 RecipientDto 목록 생성
     * {아이이름} 템플릿 변수 치환 + MOMMYTALK 버튼 생성
     */
    private List<KakaoFriendTalkRequestDto.RecipientDto> buildPersonalizedRecipientsForMommyTalk(
        List<MessageLogDetail> mldList,
        String voiceUrl
    ) {
        return mldList.stream()
            .map(mld -> {
                User user = mld.getUser();
                MessageContent content = mld.getMessageContent();

                // {아이이름} 템플릿 변수 치환
                String childName = user.getChildName() != null ? user.getChildName() : "아이이름";
                String personalizedContent = content.getContent().replace("{아이이름}", childName);

                // MOMMYTALK 버튼 생성 (가로 배치)
                List<ButtonDto> buttons = createButtonsForMommyTalk(voiceUrl, content.getId());

                return new KakaoFriendTalkRequestDto.RecipientDto(
                    user.getPhoneNumber().getPhoneNumber(),
                    personalizedContent,
                    buttons,
                    null,
                    null
                );
            })
            .toList();
    }

    /**
     * MOMMYVOCA용 개인화된 RecipientDto 목록 생성
     * {아이이름} 템플릿 변수 치환 + MOMMYVOCA 버튼 생성
     */
    private List<KakaoFriendTalkRequestDto.RecipientDto> buildPersonalizedRecipientsForMommyVoca(
        List<MessageLogDetail> mldList,
        String voiceUrl,
        String mommyVocaUrl
    ) {
        return mldList.stream()
            .map(mld -> {
                User user = mld.getUser();
                MessageContent content = mld.getMessageContent();

                // {아이이름} 템플릿 변수 치환
                String childName = user.getChildName() != null ? user.getChildName() : "아이이름";
                String personalizedContent = content.getContent().replace("{아이이름}", childName);

                // MOMMYVOCA 버튼 생성 (세로 배치)
                List<ButtonDto> buttons = createButtonsForMommyVoca(voiceUrl, mommyVocaUrl, content.getId());

                return new KakaoFriendTalkRequestDto.RecipientDto(
                    user.getPhoneNumber().getPhoneNumber(),
                    personalizedContent,
                    buttons,
                    null,
                    null
                );
            })
            .toList();
    }

}
