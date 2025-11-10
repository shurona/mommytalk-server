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


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendKakaoMessageByReservationByMessageIds(
        List<Long> messageIds, List<ReservationStatus> statusList) {

        List<MessageLog> kakaoMessageByIds = messageRepository.findMessageByIds(messageIds);

        for (MessageLog messageLog : kakaoMessageByIds) {

            // messageLogId가 동일하고 예약 상태인 messageLogDetail 목록을 갖고 온다.
            List<MessageLogDetail> mldList = messageLogDetailQueryRepository
                .findMldListByStatusWithKakao(messageLog.getId(), statusList);

            // MessageContent.id를 기준으로 전화번호 목록 생성
            Map<Long, List<String>> phoneNumbersByMessageContentId = groupPhoneNumbersByMessageContentId(
                mldList);

            // MessageContent.id를 기준으로 MessageContent 객체 Map 생성
            Map<Long, MessageContent> mldByMessageContentId = groupMldByMessageContentId(mldList);

            for (Long messageContentId : mldByMessageContentId.keySet()) {
                // 메시지 로그의 상품 정보가 있는 경우 그에 맞춰서 로직을 수행되게 한다.
                int sendStatus;
                switch (messageLog.getEntitlement().getType()) {
                    // 일반 유저
                    case EntitlementType.MOMMYTALK -> {
                        // 메시지 전송
                        sendStatus = sendMessageToKakao(
                            messageLog.getChannel(),  // 전송될 채널 정보
                            // 전송될 MessageContent에 해당하는 전화번호 목록
                            phoneNumbersByMessageContentId.get(messageContentId),
                            // 메시지 Content에 해당하는 MessageLogDetail Info
                            mldByMessageContentId.get(messageContentId),
                            getReserveTimeIfPassed(messageLog)
                        );

                    }
                    // 마미 보카
                    case EntitlementType.MOMMYVOCA -> {
                        sendStatus = sendMessageToKakaoWithDiary(
                            messageLog.getChannel(),  // 전송될 채널 정보
                            // 전송될 MessageContent에 해당하는 전화번호 목록
                            phoneNumbersByMessageContentId.get(messageContentId),
                            // 메시지 Content에 해당하는 MessageLogDetail Info
                            mldByMessageContentId.get(messageContentId),
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
            KakaoFriendTalkRequestDto requestBody;

            // 3개 버튼: mommy voice, child voice, 플래시카드
            String mommyVoice = Optional.ofNullable(content.getHeaderOneLink())
                .map(ElevenLabsMedia::getFileUrl)
                .orElse(null);
            String childVoice = Optional.ofNullable(content.getHeaderTwoLink())
                .map(ElevenLabsMedia::getFileUrl)
                .orElse(null);
            String flashCard = content.getDiaryUrl();

            // 버튼 목록 생성
            List<ButtonDto> buttons = createButtons(mommyVoice, childVoice, flashCard);

            if (buttons.isEmpty()) {
                // 버튼이 없으면 일반 메시지로 전송
                requestBody = KakaoFriendTalkRequestDto.ofMulti(
                    senderKey,
                    testPhoneNumbers,
                    content.getContent(),
                    null
                );
            } else {
                // 버튼이 있으면 버튼 메시지로 전송
                requestBody = KakaoFriendTalkRequestDto.ofWithButtons(
                    senderKey,
                    testPhoneNumbers,
                    content.getContent(),
                    null,
                    buttons
                );
            }

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

        if (response.failCount() != null && response.failCount() > 0) {
            log.warn("[Kakao 발송 실패 상세] {}", response.recipientList());
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
     * 메시지를 카카오에 전달한다.
     */
    private int sendMessageToKakao(
        Channel channel, List<String> phoneNumberList,
        MessageContent content, LocalDateTime reserveTime) {

        String senderKey = channel.getKakaoSenderKey();
        // 목록 및 senderKey가 비어 있으면 보내지 않는다.
        if (phoneNumberList.isEmpty() || senderKey == null || senderKey.isBlank()) {
            return SEND_SUCCESS;
        }

        // 카카오 메시지 전송
        for (int i = 0; i < phoneNumberList.size(); i += CHUNK_SIZE) {
            List<String> subList = phoneNumberList.subList(i,
                Math.min(i + CHUNK_SIZE, phoneNumberList.size()));
            try {
                KakaoFriendTalkRequestDto requestBody;

                // 3개 버튼: mommy voice, child voice, 플래시카드
                String mommyVoice = content.getHeaderOneLink().getFileUrl();
                String childVoice = Optional.ofNullable(content.getHeaderTwoLink())
                    .map(ElevenLabsMedia::getFileUrl)
                    .orElse(null);
                String flashCard = content.getDiaryUrl();

                // 버튼 목록 생성
                List<ButtonDto> buttons = createButtons(mommyVoice, childVoice, flashCard);

                if (buttons.isEmpty()) {
                    // 버튼이 없으면 일반 메시지로 전송
                    requestBody = KakaoFriendTalkRequestDto.ofMulti(
                        senderKey,
                        subList,
                        content.getContent(),
                        reserveTime.format(DATE_FORMATTER)
                    );
                } else {
                    // 버튼이 있으면 버튼 메시지로 전송
                    requestBody = KakaoFriendTalkRequestDto.ofWithButtons(
                        senderKey,
                        subList,
                        content.getContent(),
                        reserveTime.format(DATE_FORMATTER),
                        buttons
                    );
                }

//                KakaoFriendTalkResponseDto response = nhnKakaoMessageClient.sendMessage(
//                    kakaoSecretKey,
//                    requestBody
//                );
//
//                if (!logResponse(response)) {
//                    return SEND_FAIL;
//                }
            } catch (RestClientResponseException e) {
                log.error("[전송 중 에러 발생] {} 번째에서 에러 발생 {} 전화번호 목록 \n에러 원인 {}",
                    i, phoneNumberList, e.getMessage());
                return SEND_FAIL;
            }

            // thread sleep
            sleepForRateLimit();
        }

        return SEND_SUCCESS;
    }

    /**
     * 메시지를 마미보카 버튼과 함께 카카오에 전달한다.
     */
    private int sendMessageToKakaoWithDiary(
        Channel channel, List<String> phoneNumberList,
        MessageContent content, LocalDateTime reserveTime) {

        String senderKey = channel.getKakaoSenderKey();
        // 목록 및 senderKey가 비어 있으면 보내지 않는다.
        if (phoneNumberList.isEmpty() || senderKey == null || senderKey.isBlank()) {
            return SEND_SUCCESS;
        }

        // 카카오 메시지 전송
        for (int i = 0; i < phoneNumberList.size(); i += CHUNK_SIZE) {
            List<String> subList = phoneNumberList.subList(i,
                Math.min(i + CHUNK_SIZE, phoneNumberList.size()));
            try {
                KakaoFriendTalkRequestDto requestBody;

                // 3개 버튼: mommy voice, child voice, 플래시카드
                String mommyVoice = content.getHeaderOneLink().getFileUrl();
                String childVoice = Optional.ofNullable(content.getHeaderTwoLink())
                    .map(ElevenLabsMedia::getFileUrl)
                    .orElse(null);
                String flashCard = content.getDiaryUrl();

                // 버튼 목록 생성
                List<ButtonDto> buttons = createButtons(mommyVoice, childVoice, flashCard);

                if (buttons.isEmpty()) {
                    // 버튼이 없으면 일반 메시지로 전송
                    requestBody = KakaoFriendTalkRequestDto.ofMulti(
                        senderKey,
                        subList,
                        content.getContent(),
                        reserveTime.format(DATE_FORMATTER)
                    );
                } else {
                    // 버튼이 있으면 버튼 메시지로 전송
                    requestBody = KakaoFriendTalkRequestDto.ofWithButtons(
                        senderKey,
                        subList,
                        content.getContent(),
                        reserveTime.format(DATE_FORMATTER),
                        buttons
                    );
                }

                KakaoFriendTalkResponseDto response = nhnKakaoMessageClient.sendMessage(
                    kakaoSecretKey,
                    requestBody
                );

                if (!logResponse(response)) {
                    return SEND_FAIL;
                }
            } catch (RestClientResponseException e) {
                log.error("[전송 중 에러 발생] {} 번째에서 에러 발생 {} 전화번호 목록 \n에러 원인 {}",
                    i, phoneNumberList, e.getMessage());
                return SEND_FAIL;
            }

            // thread sleep
            sleepForRateLimit();
        }

        return SEND_SUCCESS;
    }

    /**
     * 3개 버튼 생성: mommy voice, child voice, 플래시카드 (LINE 스타일과 동일)
     */
    private List<ButtonDto> createButtons(String mommyVoice, String childVoice, String flashCard) {
        List<ButtonDto> buttons = new java.util.ArrayList<>();

        // 1. Mommy Voice 버튼
        if (mommyVoice != null && !mommyVoice.trim().isEmpty()) {
            buttons.add(new ButtonDto(
                "1",                    // ordering
                "WL",                   // type: 웹링크
                "mommy voice",          // name
                mommyVoice.trim(),      // linkMo
                mommyVoice.trim(),      // linkPc
                null,                   // schemeIos
                null                    // schemeAndroid
            ));
        }

        // 2. Child Voice 버튼
        if (childVoice != null && !childVoice.trim().isEmpty()) {
            buttons.add(new ButtonDto(
                "2",                    // ordering
                "WL",                   // type: 웹링크
                "child voice",          // name
                childVoice.trim(),      // linkMo
                childVoice.trim(),      // linkPc
                null,                   // schemeIos
                null                    // schemeAndroid
            ));
        }

        // 3. 플래시카드 버튼
        if (flashCard != null && !flashCard.trim().isEmpty()) {
            buttons.add(new ButtonDto(
                "3",                            // ordering
                "WL",                           // type: 웹링크
                "デジタルフラッシュカード📩",    // name
                flashCard.trim(),               // linkMo
                flashCard.trim(),               // linkPc
                null,                           // schemeIos
                null                            // schemeAndroid
            ));
        }

        return buttons;
    }

    /**
     * MessageLogDetail의 목록에서 messageContentId : MessageContent 형식으로 변환해준다.
     */
    private Map<Long, MessageContent> groupMldByMessageContentId(List<MessageLogDetail> mldList) {
        return mldList.stream()
            .collect(Collectors.toMap(
                mld -> mld.getMessageContent().getId(),
                MessageLogDetail::getMessageContent,
                (existing, replacement) -> existing // 이건 같은 것이 나오면 대체하냐의 옵션
            ));
    }

    /**
     * 전송될 메시지 콘텐츠에 해당하는 전화번호 목록을 반환한다.
     */
    private Map<Long, List<String>> groupPhoneNumbersByMessageContentId(
        List<MessageLogDetail> mldList) {
        return mldList.stream()
            .collect(Collectors.groupingBy(
                mld -> mld.getMessageContent().getId(),
                Collectors.mapping(
                    mld -> mld.getUser().getPhoneNumber().getPhoneNumber(),
                    // .toString() → .getPhoneNumber()
                    Collectors.toList()
                )
            ));
    }

    private LocalDateTime getReserveTimeIfPassed(MessageLog messageLog) {
        return LocalDateTime.now().isAfter(messageLog.getReserveTime())
            ? LocalDateTime.now().plusHours(9).plusSeconds(10)// 약간 뒤의 시간으로 예약한다.
            : messageLog.getReserveTime().plusHours(9);
    }

}
