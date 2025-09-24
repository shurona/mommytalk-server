package com.shrona.mommytalk.line.application.sender;

import static com.shrona.mommytalk.message.domain.type.ReservationStatus.COMPLETE;
import static com.shrona.mommytalk.message.domain.type.ReservationStatus.FAIL;
import static com.shrona.mommytalk.message.domain.type.ReservationStatus.PREPARE;

import com.shrona.mommytalk.admin.application.AdminService;
import com.shrona.mommytalk.admin.presentation.form.TestUserForm;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.line.infrastructure.sender.LineMessageSenderClient;
import com.shrona.mommytalk.line.infrastructure.sender.LineMessageSingleSenderClient;
import com.shrona.mommytalk.line.infrastructure.sender.dto.LineMessageMulticastRequestBody;
import com.shrona.mommytalk.line.infrastructure.sender.dto.LineMessageSingleRequestBody;
import com.shrona.mommytalk.line.infrastructure.sender.dto.flex.ActionDto;
import com.shrona.mommytalk.line.infrastructure.sender.dto.flex.BoxTypeDto;
import com.shrona.mommytalk.line.infrastructure.sender.dto.flex.BubbleMessageDto;
import com.shrona.mommytalk.line.infrastructure.sender.dto.flex.ButtonTypeDto;
import com.shrona.mommytalk.line.infrastructure.sender.dto.flex.ContentType;
import com.shrona.mommytalk.line.infrastructure.sender.dto.flex.LineFlexMessageRequestDto;
import com.shrona.mommytalk.line.infrastructure.sender.dto.flex.TextContentDto;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.message.domain.MessageTemplate;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageLogDetailQueryRepository;
import com.shrona.mommytalk.message.infrastructure.repository.query.MessageQueryRepository;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Service
public class LineMessageSenderImpl implements LineMessageSender {

    // 메시지 전송 status
    private static final int SEND_FAIL = 0;
    private static final int SEND_SUCCESS = 1;
    // multi send의 chunk size
    private static final int CHUNK_SIZE = 500;
    private static final String prefixHeader = "Bearer ";
    // restClient
    private final LineMessageSenderClient lineMessageSenderClient;
    private final LineMessageSingleSenderClient lineMessageSingleSenderClient;
    // repository
    private final MessageQueryRepository messageRepository;
    private final MessageLogDetailQueryRepository messageLogDetailQueryRepository;
    private final AdminService adminService;

    @Override
    public void sendLineMessageByReservationByMessageIds(List<Long> messageIds) {

        List<MessageLog> lineMessageByIds = messageRepository.findMessageByIds(messageIds);

        for (MessageLog messageLog : lineMessageByIds) {

            // 예약 상태인 messageLogDetail 목록을 갖고 온다.
            messageLogDetailQueryRepository.findMldiListByStatusWithLine(
                messageLog.getId(), PREPARE);

            List<MessageLogDetail> mldiList = messageLog.getMessageLogDetailList();

            // MessageTemplate.id를 기준으로 LineId 목록 생성
            Map<Long, List<String>> lineIdsBySmtId = mldiList.stream()
                .collect(Collectors.groupingBy(
                    mldi -> mldi.getMessageTemplate().getId(),
                    Collectors.mapping(
                        mldi -> mldi.getUser().getLineUser().getLineId(),
                        Collectors.toList()
                    )
                ));

            // MessageTemplate.id를 기준으로 MessageTemplate 객체 맵 생성
            Map<Long, MessageTemplate> messageTemplateById = mldiList.stream()
                .collect(Collectors.toMap(
                    mldi -> mldi.getMessageTemplate().getId(),
                    MessageLogDetail::getMessageTemplate,
                    (existing, replacement) -> existing
                ));

            for (Long smtId : messageTemplateById.keySet()) {
                // 메시지 전송
                int sendStatus = sendMessageToLine(
                    messageLog.getChannel(), lineIdsBySmtId.get(smtId),
                    messageTemplateById.get(smtId));
                // 전송 성공 시 메시지 상태 변경 및 sender time 설정
                if (sendStatus == SEND_SUCCESS) {
                    // smtId인 MessageLogDetailInfo를 업데이트 해준다.
                    messageLogDetailQueryRepository.updateStatusByStmId(smtId, COMPLETE);
                } else if (sendStatus == SEND_FAIL) {
                    messageLogDetailQueryRepository.updateStatusByStmId(smtId, FAIL);
                }
            }
        }


    }

    @Override
    public boolean sendTestLineMessage(Channel channel, String text) {
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
            LineMessageMulticastRequestBody lineMessageMulticastRequestBody
                = LineMessageMulticastRequestBody.of(lineIdList, text);

            // 메시지 전송
            lineMessageSenderClient.SendMulticastMessage(prefixHeader + decodedString,
                lineMessageMulticastRequestBody);
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
    private int sendMessageToLine(
        Channel channel, List<String> lineIdList, MessageTemplate content) {

        String accessToken = channel.getAccessToken();
        // 목록 및 accessToken이 비어 있으면 보내지 않는다.
        if (lineIdList.isEmpty() || accessToken.isBlank()) {
            return SEND_SUCCESS;
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
                String headerLink = content.getHeaderOneLink();
                String bottomLink = content.getHeaderTwoLink();

                if ((headerLink != null && !headerLink.trim().isEmpty()) ||
                    (bottomLink != null && !bottomLink.trim().isEmpty())) {

                    requestBody = LineMessageMulticastRequestBody.ofFlex(subList,
                        createBubbleObj(content.getContent(), headerLink, bottomLink));
                } else {
                    // 일반 텍스트 메시지
                    requestBody = LineMessageMulticastRequestBody.of(subList,
                        content.getContent());
                }

                lineMessageSenderClient.SendMulticastMessage(
                    prefixHeader + decodedString,
                    requestBody
                );
            } catch (RestClientResponseException e) {
                //TODO : 어떻게 처리할까
                log.error("[전송 중 에러 발생] {} 번째에서 에러 발생 {} id 목록 \n에러 원인 {}",
                    i, lineIdList, e.getMessage());
                return SEND_FAIL;
            }

            // thread sleep
            sleepThreadForRateLimit();
        }

        return SEND_SUCCESS;
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

    private LineFlexMessageRequestDto createBubbleObj(String text, String headerLink,
        String bottomLink) {
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

        return new LineFlexMessageRequestDto(
            ContentType.FLEX,
            text.substring(0, 100),
            bubbleMessage
        );
    }
}
