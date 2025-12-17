package com.shrona.mommytalk.kakao.application.sender;

import static com.shrona.mommytalk.message.domain.type.ReservationStatus.COMPLETE;
import static com.shrona.mommytalk.message.domain.type.ReservationStatus.FAIL;

import com.shrona.mommytalk.admin.application.AdminService;
import com.shrona.mommytalk.admin.presentation.form.TestUserServiceDto;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.elevenlabs.domain.ElevenLabsMedia;
import com.shrona.mommytalk.entitlement.domain.EntitlementType;
import com.shrona.mommytalk.kakao.domain.type.KakaoAlimtalkTemplate;
import com.shrona.mommytalk.kakao.infrastructure.sender.NhnAlimtalkMessageClient;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.AlimtalkMessageRequestDto;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.AlimtalkMessageRequestDto.RecipientDto;
import com.shrona.mommytalk.kakao.infrastructure.sender.dto.AlimtalkMessageResponseDto;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoAlimtalkSenderImpl implements KakaoMessageSender {

    // 메시지 전송 status
    private static final int SEND_FAIL = 0;
    private static final int SEND_SUCCESS = 1;
    private static final int CHUNK_SIZE = 1000; // 알림톡 API 최대 수신자 수
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm");

    // restClient
    private final NhnAlimtalkMessageClient nhnAlimtalkMessageClient;
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
                        sendAlimtalkToKakao(
                            messageLog.getChannel(),
                            contentMldList,
                            getReserveTimeIfPassed(messageLog),
                            KakaoAlimtalkTemplate.MOMMYTALK365
                        );
                    }
                    // 마미 보카
                    case EntitlementType.MOMMYVOCA -> {
                        // 메시지 전송 (내부에서 상태 업데이트 완료)
                        sendAlimtalkToKakao(
                            messageLog.getChannel(),
                            contentMldList,
                            getReserveTimeIfPassed(messageLog),
                            KakaoAlimtalkTemplate.MOMMYTALK365_PREMIUM
                        );
                    }
                }
            }
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

        log.info("[Kakao 알림톡 테스트 메시지] 발송 전화번호 목록: {}", testPhoneNumbers);

        if (testPhoneNumbers.isEmpty()) {
            log.warn("[Kakao 알림톡 테스트 메시지] 테스트 유저가 없습니다.");
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
            KakaoAlimtalkTemplate template = (mommyVocaUrl != null
                && !mommyVocaUrl.trim().isEmpty())
                ? KakaoAlimtalkTemplate.MOMMYTALK365_PREMIUM
                : KakaoAlimtalkTemplate.MOMMYTALK365;

            String personalizedContent = content.getContent().replace("{아이이름}", "아이는");

            // 수신자별 templateParameter 생성
            List<RecipientDto> recipientList = testPhoneNumbers.stream()
                .map(phone -> {
                    Map<String, String> templateParameter = createTemplateParameter(
                        personalizedContent,
                        voiceUrl,
                        mommyVocaUrl,
                        152L,  // 테스트용 고정 ID
                        template
                    );
                    return RecipientDto.of(phone, templateParameter);
                })
                .toList();

            AlimtalkMessageRequestDto requestBody = AlimtalkMessageRequestDto.of(
                senderKey,
                template.getTemplateCode(),
                recipientList,
                null  // 즉시 전송
            );

            AlimtalkMessageResponseDto response = nhnAlimtalkMessageClient.sendMessage(
                kakaoSecretKey,
                requestBody
            );

            boolean isSuccessful = logResponse(response);
            return isSuccessful;
        } catch (Exception e) {
            log.error("[Kakao 알림톡 테스트 메시지 에러] 에러: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 알림톡 메시지를 카카오에 전달한다. (개인화 지원)
     * {아이이름} 템플릿 변수 치환 + 버튼 URL 동적 주입
     * <p>
     * 알림톡은 개인화를 지원하므로 수신자 배열로 한 번에 전송 (최대 1000명)
     */
    private int sendAlimtalkToKakao(
        Channel channel,
        List<MessageLogDetail> mldList,
        LocalDateTime reserveTime,
        KakaoAlimtalkTemplate template) {

        String senderKey = channel.getKakaoSenderKey();
        if (mldList.isEmpty() || senderKey == null || senderKey.isBlank()) {
            return SEND_SUCCESS;
        }

        // 첫 번째 MessageContent에서 발음듣기 URL, 마미보카 URL 추출
        MessageContent firstContent = mldList.get(0).getMessageContent();
        String voiceUrl = Optional.ofNullable(firstContent.getHeaderOneLink())
            .map(ElevenLabsMedia::getFileUrl)
            .orElse(null);
        String mommyVocaUrl = firstContent.getMommyVoca();

        String requestDate = reserveTime.format(DATE_FORMATTER);

        // 성공/실패 ID 수집용 리스트
        List<Long> successIds = new ArrayList<>();
        List<Long> failIds = new ArrayList<>();

        // 1000명씩 chunk로 나누어 전송
        for (int i = 0; i < mldList.size(); i += CHUNK_SIZE) {
            List<MessageLogDetail> chunk = mldList.subList(
                i,
                Math.min(i + CHUNK_SIZE, mldList.size())
            );

            // 수신자별 개인화 데이터 생성
            List<RecipientDto> recipientList = chunk.stream()
                .map(mld -> {
                    User user = mld.getUser();
                    MessageContent content = mld.getMessageContent();

                    // {아이이름} 템플릿 변수 치환
                    String childName = user.getChildName() != null ? user.getChildName() : "아이는";
                    String personalizedContent = content.getContent()
                        .replace("{아이이름}", childName);

                    // templateParameter 생성 (메시지 + 버튼 URL)
                    Map<String, String> templateParameter = createTemplateParameter(
                        personalizedContent,
                        voiceUrl,
                        mommyVocaUrl,
                        mld.getId(),
                        template
                    );

                    return RecipientDto.of(user.getPhoneNumber().getPhoneNumber(),
                        templateParameter);
                })
                .toList();

            try {
                AlimtalkMessageRequestDto request = AlimtalkMessageRequestDto.of(
                    senderKey,
                    template.getTemplateCode(),
                    recipientList,
                    requestDate
                );

                AlimtalkMessageResponseDto response = nhnAlimtalkMessageClient.sendMessage(
                    kakaoSecretKey,
                    request
                );

                // 응답에서 성공/실패 ID 분류
                if (response.message() != null && response.message().sendResults() != null) {
                    for (int j = 0; j < response.message().sendResults().size(); j++) {
                        AlimtalkMessageResponseDto.SendResult result = response.message()
                            .sendResults().get(j);
                        MessageLogDetail mld = chunk.get(j);

                        if (result.resultCode() != null && result.resultCode() == 0) {
                            successIds.add(mld.getId());
                        } else {
                            failIds.add(mld.getId());
                        }
                    }
                }

                logResponse(response);
            } catch (RestClientResponseException ex) {
                log.error("[알림톡 전송 에러] chunk 번호: {}, 에러: {}", i / CHUNK_SIZE, ex.getMessage());
                // chunk 전체를 실패로 처리
                chunk.forEach(mld -> failIds.add(mld.getId()));
            }
        }

        // Batch 업데이트: 성공/실패 ID 목록으로 상태 변경
        if (!successIds.isEmpty()) {
            messageLogDetailQueryRepository.updateStatusByIds(successIds, COMPLETE);
        }
        if (!failIds.isEmpty()) {
            messageLogDetailQueryRepository.updateStatusByIds(failIds, FAIL);
        }

        log.info("[알림톡 개인화 전송 완료] 템플릿: {}, 성공: {}, 실패: {}",
            template.getDescription(), successIds.size(), failIds.size());
        return failIds.isEmpty() ? SEND_SUCCESS : SEND_FAIL;
    }

    /**
     * 템플릿 변수 맵 생성
     * - 오늘의엄마표영어: 메시지 내용
     * - 발음안내링크: 발음듣기 버튼 URL
     * - 단어안내링크: 마미보카 버튼 URL (PREMIUM만 해당)
     */
    private Map<String, String> createTemplateParameter(
        String personalizedContent,
        String voiceUrl,
        String mommyVocaUrl,
        Long messageLogDetailId,
        KakaoAlimtalkTemplate template
    ) {
        Map<String, String> templateParameter = new HashMap<>();

        // 메시지 내용
        templateParameter.put("오늘의엄마표영어", personalizedContent);

        // 발음듣기 버튼 URL
        if (voiceUrl != null && !voiceUrl.trim().isEmpty()) {
            String voiceLink = frontBaseUrl + "mommytalk365/" + messageLogDetailId;
            templateParameter.put("발음안내링크", voiceLink);
        }

        // 마미보카 버튼 URL (PREMIUM일 때만 추가)
        if (template == KakaoAlimtalkTemplate.MOMMYTALK365_PREMIUM
            && mommyVocaUrl != null && !mommyVocaUrl.trim().isEmpty()) {
            templateParameter.put("단어안내링크", mommyVocaUrl.trim());
        }

        return templateParameter;
    }

    /**
     * 응답 로깅 (Alimtalk API)
     */
    private Boolean logResponse(AlimtalkMessageResponseDto response) {
        boolean isSuccessful = response.header() != null &&
            Boolean.TRUE.equals(response.header().isSuccessful());

        int successCount = 0;
        int failCount = 0;

        if (response.message() != null && response.message().sendResults() != null) {
            for (AlimtalkMessageResponseDto.SendResult result : response.message()
                .sendResults()) {
                if (result.resultCode() != null && result.resultCode() == 0) {
                    successCount++;
                } else {
                    failCount++;
                }
            }
        }

        if (!isSuccessful) {
            log.warn("[Kakao 알림톡 발송 실패 상세] {}", response);
        }

        return isSuccessful;
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
