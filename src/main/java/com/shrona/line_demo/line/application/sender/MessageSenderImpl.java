package com.shrona.line_demo.line.application.sender;

import com.shrona.line_demo.admin.application.AdminService;
import com.shrona.line_demo.admin.domain.AdminUser;
import com.shrona.line_demo.line.application.MessageService;
import com.shrona.line_demo.line.domain.MessageLog;
import com.shrona.line_demo.line.infrastructure.sender.LineMessageSenderClient;
import com.shrona.line_demo.line.infrastructure.sender.dto.LineMessageMulticastRequestBody;
import com.shrona.line_demo.user.application.GroupService;
import com.shrona.line_demo.user.domain.Group;
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

    // restClient
    private final LineMessageSenderClient lineMessageSenderClient;

    // Service
    private final MessageService messageService;
    private final GroupService groupService;
    private final AdminService adminService;


    @Transactional
    public void sendLineMessageByReservation() {

        // Message 중에 sender가 이전 및 대기 중인 메시지 목록을 조회
        List<MessageLog> reservedMessage = messageService.findReservedMessage();

        for (MessageLog messageLog : reservedMessage) {
            // 그룹 목록에서 User의 lineId를 추출
            Group groupInfo = groupService.findGroupById(messageLog.getGroup().getId(), true);

            List<String> lineIdList = groupInfo.getUserGroupList().stream()
                .map(gu -> gu.getUser().getLineId()).toList();

            // 라인 메시지 전송
            try {
                lineMessageSenderClient.SendMulticastMessage(
                    LineMessageMulticastRequestBody.of(lineIdList, messageLog.getContent()));
            } catch (RestClientResponseException e) {
                // TODO: 어떻게 처리할까
                log.error("에러 발생 : " + e.getMessage());
                continue;
            }

            // 전송 완료 시 메시지 상태 변경 및 sender time 설정
            messageLog.changeStatusAfterSend();
        }
    }

    @Override
    public boolean sendTestLineMessage(String text) {

        // Admin 라인 채널을 갖고 온다.
        List<String> lineIdList = adminService.findAdminUserList().stream()
            .map(AdminUser::getLineId)
            .filter(l -> l != null && !l.isEmpty())
            .toList();

        System.out.println(lineIdList);

        // 테스트용 라인 메시지
        try {
            LineMessageMulticastRequestBody lineMessageMulticastRequestBody = LineMessageMulticastRequestBody.of(
                lineIdList, text);

            // 메시지 전송
            lineMessageSenderClient.SendMulticastMessage(lineMessageMulticastRequestBody);
        } catch (Exception e) {
            log.error("에러 발생 : " + e.getMessage());
            return false;
        }

        return true;
    }
}
