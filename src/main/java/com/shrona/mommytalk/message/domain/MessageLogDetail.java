package com.shrona.mommytalk.message.domain;

import com.shrona.mommytalk.message.domain.type.ReservationStatus;
import com.shrona.mommytalk.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "message_log_detail")
public class MessageLogDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column
    private ReservationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_log")
    private MessageLog messageLog;

    @Column(name = "sent_time")
    private LocalDateTime sentTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_template_id")
    private MessageTemplate messageTemplate;

    public static MessageLogDetail createLogDetail(
        MessageLog messageLog, User user, MessageTemplate messageTemplate) {
        MessageLogDetail messageLogDetail = new MessageLogDetail();
        messageLogDetail.status = ReservationStatus.PREPARE;
        messageLogDetail.messageLog = messageLog;
        messageLogDetail.user = user;
        messageLogDetail.messageTemplate = messageTemplate;
        return messageLogDetail;
    }

    /**
     * 전송 후 메시지 성공 상태로 변경
     */
    public void changeStatusAfterSend() {
        this.status = ReservationStatus.COMPLETE;
        this.sentTime = LocalDateTime.now();
    }

    public void setMessageLogInfo(MessageLog log) {
        this.messageLog = log;
    }

    /**
     * 전송 실패 시 업데이트
     */
    public void failedStatusBecauseError() {
        this.status = ReservationStatus.FAIL;
        this.sentTime = LocalDateTime.now();
    }
}
