package com.shrona.line_demo.line.application.sender;

import com.shrona.line_demo.admin.application.AdminService;
import com.shrona.line_demo.admin.presentation.form.TestUserForm;
import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.line.domain.MessageLog;
import com.shrona.line_demo.line.domain.MessageLogLineInfo;
import com.shrona.line_demo.line.domain.type.ReservationStatus;
import com.shrona.line_demo.line.infrastructure.MessageLogJpaRepository;
import com.shrona.line_demo.line.infrastructure.sender.LineMessageSenderClient;
import com.shrona.line_demo.line.infrastructure.sender.LineMessageSingleSenderClient;
import com.shrona.line_demo.line.infrastructure.sender.dto.LineMessageMulticastRequestBody;
import com.shrona.line_demo.line.infrastructure.sender.dto.LineMessageSingleRequestBody;
import com.shrona.line_demo.line.infrastructure.sender.dto.flex.ActionDto;
import com.shrona.line_demo.line.infrastructure.sender.dto.flex.BoxTypeDto;
import com.shrona.line_demo.line.infrastructure.sender.dto.flex.BubbleMessageDto;
import com.shrona.line_demo.line.infrastructure.sender.dto.flex.ButtonTypeDto;
import com.shrona.line_demo.line.infrastructure.sender.dto.flex.ContentType;
import com.shrona.line_demo.line.infrastructure.sender.dto.flex.LineFlexMessageRequestDto;
import com.shrona.line_demo.line.infrastructure.sender.dto.flex.TextContentDto;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Service
public class MessageSenderImpl implements MessageSender {

    // 메시지 전송 status
    private static final int FAIL = 0;
    private static final int SUCCESS = 1;
    // multi send의 chunk size
    private static final int CHUNK_SIZE = 500;
    private static final String prefixHeader = "Bearer ";
    // restClient
    private final LineMessageSenderClient lineMessageSenderClient;
    private final LineMessageSingleSenderClient lineMessageSingleSenderClient;
    // repository
    private final MessageLogJpaRepository messageRepository;
    private final AdminService adminService;

    @Transactional
    public void sendLineMessageByReservation() {

        // Message 중에 sender가 이전 및 대기 중인 메시지 목록을 조회
        List<MessageLog> reservedMessage =
            messageRepository.findAllByReservedMessageBeforeNow(LocalDateTime.now());

        for (MessageLog messageLog : reservedMessage) {
            // 메시지 전송
            int sendStatus = sendMessageToLine(messageLog);

            // 전송 성공 시 메시지 상태 변경 및 sender time 설정
            if (sendStatus == SUCCESS) {
                messageLog.changeStatusAfterSend();
            } else if (sendStatus == FAIL) {
                messageLog.failedStatusBecauseError();
            }
        }
    }

    @Transactional
    public void sendLineMessageByReservationByMessageIds(List<Long> messageIds) {
        log.info("발송 시작  : {}", messageIds);
        List<MessageLog> messageLogList = messageRepository.findAllById(messageIds);
        for (MessageLog messageLog : messageLogList) {
            // 예약 상태가 아니면 패스
            if (!messageLog.getStatus().equals(ReservationStatus.PREPARE)) {
                continue;
            }

            // 메시지 전송
            int sendStatus = sendMessageToLine(messageLog);

            // 전송 성공 시 메시지 상태 변경 및 sender time 설정
            if (sendStatus == SUCCESS) {
                messageLog.changeStatusAfterSend();
            } else if (sendStatus == FAIL) {
                messageLog.failedStatusBecauseError();
            }
        }
    }

    @Override
    public boolean sendTestLineMessage(
        Channel channel, String text, String headerLink, String bottomLink) {

        // 테스트 유저 목록을 갖고 온다.
        List<String> lineIdList = adminService.findAllTestUser(channel)
            .stream().map(TestUserForm::lineId)
            .filter(s -> !s.isBlank()).toList();

        log.info("[테스트 메시지] 메시지 발송 아이디 목록 {}", lineIdList);

        if (lineIdList.isEmpty()) {
            return true;
        }

        String accessToken = channel.getAccessToken();

        // accessToken을 Base64 -> utf8로 변환한다.
        String decodedString = base64ToUtf8(accessToken);

        // 테스트용 라인 메시지
        try {
            LineMessageMulticastRequestBody requestBody;

            // 헤더링크나 푸터링크가 있으면 Flex 메시지로 전송
            if ((headerLink != null && !headerLink.trim().isEmpty()) ||
                (bottomLink != null && !bottomLink.trim().isEmpty())) {

                requestBody = LineMessageMulticastRequestBody.ofFlex(lineIdList,
                    createBubbleObj(text, headerLink, bottomLink));
            } else {
                // 일반 텍스트 메시지
                requestBody = LineMessageMulticastRequestBody.of(lineIdList, text);
            }

            // 메시지 전송
            lineMessageSenderClient.SendMulticastMessage(prefixHeader + decodedString, requestBody);
        } catch (Exception e) {
            log.error("에러 발생 : " + e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public void sendSingleMessageWithContents(Channel channel, LineUser lineUser, String text) {

        String accessToken = channel.getAccessToken();
        String decodeToken = base64ToUtf8(accessToken);
        try {
            lineMessageSingleSenderClient.sendSingleMessage(prefixHeader + decodeToken,
                LineMessageSingleRequestBody.of(lineUser.getLineId(), text));
        } catch (Exception e) {
            // TODO: 어떻게 처리할까
            log.error("[단일 전송 에러] 에러 원인 {}", e.getMessage());
        }
        // thread sleep
        sleepThreadForRateLimit();
    }

    /**
     * 메시지를 라인에 전달한다.
     */
    private int sendMessageToLine(MessageLog messageLog) {
        // 그룹 목록에서 User의 lineId를 추출
        List<String> lineIdList = messageLog.getMessageLogLineInfoList().stream()
            .map(MessageLogLineInfo::getLineId).toList();

        String accessToken = messageLog.getChannel().getAccessToken();
        // 목록 및 accessToken이 비어 있으면 보내지 않는다.
        if (lineIdList.isEmpty() || accessToken.isBlank()) {
            return SUCCESS;
        }

        // accessToken을 Base64 -> utf8로 변환한다.
        String decodedString = base64ToUtf8(accessToken);

        // 라인 메시지 전송
        for (int i = 0; i < lineIdList.size(); i += CHUNK_SIZE) {
            List<String> subList = lineIdList.subList(i,
                Math.min(i + CHUNK_SIZE, lineIdList.size()));
            try {
                LineMessageMulticastRequestBody requestBody;

                // 헤더링크나 푸터링크가 있으면 Flex 메시지로 전송
                String headerLink = messageLog.getHeaderLink();
                String bottomLink = messageLog.getBottomLink();

                if ((headerLink != null && !headerLink.trim().isEmpty()) ||
                    (bottomLink != null && !bottomLink.trim().isEmpty())) {

                    requestBody = LineMessageMulticastRequestBody.ofFlex(subList,
                        createBubbleObj(messageLog.getContent(), headerLink, bottomLink));
                } else {
                    // 일반 텍스트 메시지
                    requestBody = LineMessageMulticastRequestBody.of(subList, messageLog.getContent());
                }

                requestBody = LineMessageMulticastRequestBody.ofFlex(subList,
                    createBubbleObj(messageLog.getContent(), headerLink, bottomLink));

                lineMessageSenderClient.SendMulticastMessage(
                    prefixHeader + decodedString,
                    requestBody
                );
            } catch (RestClientResponseException e) {
                //TODO : 어떻게 처리할까
                log.error("[전송 중 에러 발생] {} 번째에서 에러 발생 {} id 목록 \n에러 원인 {}",
                    i, lineIdList, e.getMessage());
                return FAIL;
            }

            // thread sleep
            sleepThreadForRateLimit();
        }

        return SUCCESS;
    }

    private String base64ToUtf8(String accessToken) {
        byte[] decodedBytes = Base64.getDecoder().decode(accessToken);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    private void sleepThreadForRateLimit() {
        try {
            Thread.sleep(10);
        } catch (Exception e) {
            //
        }
    }

    private LineFlexMessageRequestDto createBubbleObj(String text, String headerLink, String bottomLink) {
        // Body 생성
        BoxTypeDto body = new BoxTypeDto(
            ContentType.BOX,
            "vertical",
            List.of(new TextContentDto(ContentType.TEXT, text, true))
        );

        // Header 생성 (헤더링크가 있을 때만)
        BoxTypeDto header = null;
        if (headerLink != null && !headerLink.trim().isEmpty()) {
            header = new BoxTypeDto(
                ContentType.BOX,
                "vertical",
                List.of(
                    new ButtonTypeDto(
                        ContentType.BUTTON, "primary", "sm", new ActionDto(
                        "uri", "発音を聞く\uD83D\uDD08", headerLink.trim()
                    )
                    )
                )
            );
        }

        // Footer 생성 (푸터링크가 있을 때만)
        BoxTypeDto footer = null;
        if (bottomLink != null && !bottomLink.trim().isEmpty()) {
            footer = new BoxTypeDto(
                ContentType.BOX,
                "vertical",
                List.of(new ButtonTypeDto(ContentType.BUTTON,
                    "secondary", "sm",
                    new ActionDto(
                        "uri", "デジタルフラッシュカード\uD83D\uDCE9", bottomLink.trim()
                    )
                )));
        }

        // BubbleMessageDto 생성
        BubbleMessageDto bubbleMessage = new BubbleMessageDto(
            ContentType.BUBBLE,
            header, // header
            null, // hero
            body,
            footer
        );

        LineFlexMessageRequestDto messageRequest = new LineFlexMessageRequestDto(
            ContentType.FLEX,
            text, // altText로 메시지 내용 사용
            bubbleMessage
        );

        return messageRequest;
    }
}
