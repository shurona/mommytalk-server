package com.shrona.mommytalk.message.domain;

import com.shrona.mommytalk.user.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "message_log_detail_info")
public class MessageLogDetailInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_log")
    private MessageLog messageLog;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_message_text_id")
    private ScheduledMessageText scheduledMessageText;

    public static MessageLogDetailInfo createLogDetail(
        MessageLog messageLog, User user, ScheduledMessageText scheduledMessageText) {
        MessageLogDetailInfo messageLogDetailInfo = new MessageLogDetailInfo();
        messageLogDetailInfo.messageLog = messageLog;
        messageLogDetailInfo.user = user;
        messageLogDetailInfo.scheduledMessageText = scheduledMessageText;
        return messageLogDetailInfo;
    }

    public void setMessageLogInfo(MessageLog log) {
        this.messageLog = log;
    }
}
