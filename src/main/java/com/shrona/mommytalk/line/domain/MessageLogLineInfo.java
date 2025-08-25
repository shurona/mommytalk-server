package com.shrona.mommytalk.line.domain;

import jakarta.persistence.Column;
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
@Table(name = "message_log_line_info")
public class MessageLogLineInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String lineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private MessageLog messageLog;

    public static MessageLogLineInfo createLineInfo(MessageLog messageLog, String lineId) {
        MessageLogLineInfo messageLogLineInfo = new MessageLogLineInfo();
        messageLogLineInfo.messageLog = messageLog;
        messageLogLineInfo.lineId = lineId;

        return messageLogLineInfo;
    }

    public void setMessageLogInfo(MessageLog log) {
        this.messageLog = log;
    }
}
