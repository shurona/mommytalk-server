package com.shrona.line_demo.line.application.sender;

import com.shrona.line_demo.admin.application.AdminService;
import com.shrona.line_demo.admin.domain.AdminUser;
import com.shrona.line_demo.line.domain.MessageLog;
import com.shrona.line_demo.line.domain.type.ReservationStatus;
import com.shrona.line_demo.line.infrastructure.MessageLogJpaRepository;
import com.shrona.line_demo.line.infrastructure.sender.LineMessageSenderClient;
import com.shrona.line_demo.line.infrastructure.sender.dto.LineMessageMulticastRequestBody;
import com.shrona.line_demo.user.application.GroupService;
import com.shrona.line_demo.user.domain.Group;
import java.time.LocalDateTime;
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

    // status int
    private static final int FAIL = 0;
    private static final int SUCCESS = 1;
    // restClient
    private final LineMessageSenderClient lineMessageSenderClient;
    // Service
    private final GroupService groupService;
    private final AdminService adminService;
    // repository
    private final MessageLogJpaRepository messageRepository;

    @Transactional
    public void sendLineMessageByReservation() {

        // Message 중에 sender가 이전 및 대기 중인 메시지 목록을 조회
        List<MessageLog> reservedMessage =
            messageRepository.findAllByReservedMessage(LocalDateTime.now());

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
    public boolean sendTestLineMessage(String text) {

        // Admin 라인 채널을 갖고 온다.
        List<String> lineIdList = adminService.findAdminUserList().stream()
            .map(AdminUser::getLineId)
            .filter(l -> l != null && !l.isEmpty())
            .toList();

        log.info("[테스트 메시지] 메시지 발송 아이디 목록 {}", lineIdList);

        // 테스트용 라인 메시지
        try {
            LineMessageMulticastRequestBody lineMessageMulticastRequestBody
                = LineMessageMulticastRequestBody.of(lineIdList, text);

            // 메시지 전송
            lineMessageSenderClient.SendMulticastMessage(lineMessageMulticastRequestBody);
        } catch (Exception e) {
            log.error("에러 발생 : " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * 메시지를 라인에 전달한다.
     */
    private int sendMessageToLine(MessageLog messageLog) {
        // 그룹 목록에서 User의 lineId를 추출
        Group groupInfo = groupService.findGroupById(messageLog.getGroup().getId(), true);
        List<String> lineIdList = groupInfo.getUserGroupList().stream()
            .filter(
                gu -> gu.getUser().getLineId() != null
                    && !gu.getUser().getLineId().isEmpty()) // 연결된 라인 아이디가 있는지 확인한다.
            .map(gu -> gu.getUser().getLineId()).toList();

        // 목록이 비어 있으면 보내지 않는다.
        if (lineIdList.isEmpty()) {
            return SUCCESS;
        }

        // 라인 메시지 전송
        try {
            lineMessageSenderClient.SendMulticastMessage(
                LineMessageMulticastRequestBody.of(lineIdList, messageLog.getContent()));
        } catch (RestClientResponseException e) {
            // TODO: 어떻게 처리할까
            log.error("에러 발생 : " + e.getMessage());
            return FAIL;
        }

        return SUCCESS;
    }
}
