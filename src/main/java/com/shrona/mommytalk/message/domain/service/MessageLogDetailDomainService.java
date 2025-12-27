package com.shrona.mommytalk.message.domain.service;

import com.shrona.mommytalk.message.application.MessageContentService;
import com.shrona.mommytalk.message.domain.MessageContent;
import com.shrona.mommytalk.message.domain.MessageLog;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import com.shrona.mommytalk.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageLogDetailDomainService {

    private final MessageContentService messageContentService;

    /**
     * 도메인 규칙: MessageLog에 누락된 유저에 대한 MessageLogDetail 생성
     * 순수 비즈니스 로직만 포함 (DB 접근 X)
     */
    public List<MessageLogDetail> createDetailsForUsers(MessageLog messageLog, List<User> users) {

        // 1. 도메인 검증
        if (!messageLog.canAddNewDetails()) {
            throw new IllegalStateException("MessageLog 상태가 변경 불가능합니다.");
        }

        // 2. MessageContent 조회
        Map<String, MessageContent> levelMap = messageContentService
            .groupMessageContentByLevel(messageLog.getMessageType());

        // 3. MessageLogDetail 생성 (도메인 로직)
        List<MessageLogDetail> details = new ArrayList<>();

        for (User user : users) {
            String levelKey = user.createKeyPropertyForMessageContent();
            MessageContent content = levelMap.get(levelKey);

            if (content != null) {
                MessageLogDetail detail = MessageLogDetail.createLogDetail(
                    messageLog, user, content
                );
                details.add(detail);
            }
        }

        return details;
    }
}
