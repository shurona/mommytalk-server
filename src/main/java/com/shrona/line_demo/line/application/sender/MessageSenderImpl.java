package com.shrona.line_demo.line.application.sender;

import com.shrona.line_demo.line.application.MessageService;
import com.shrona.line_demo.line.domain.MessageLog;
import com.shrona.line_demo.line.infrastructure.sender.LineMessageSenderClient;
import com.shrona.line_demo.line.infrastructure.sender.dto.LineMessageMulticastRequestBody;
import com.shrona.line_demo.user.application.GroupService;
import com.shrona.line_demo.user.domain.Group;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MessageSenderImpl implements MessageSender {

    private final LineMessageSenderClient lineMessageSenderClient;
    private final MessageService messageService;
    private final GroupService groupService;

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
                System.out.println("에러 발생");
                continue;
            }

            // 전송 완료 시 메시지 상태 변경 및 sender time 설정
            messageLog.changeStatusAfterSend();
        }


    }
}
