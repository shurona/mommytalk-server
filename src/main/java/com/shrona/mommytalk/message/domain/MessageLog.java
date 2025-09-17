package com.shrona.mommytalk.message.domain;

import static jakarta.persistence.CascadeType.PERSIST;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.entity.BaseEntity;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.line.common.exception.LineErrorCode;
import com.shrona.mommytalk.line.common.exception.LineException;
import com.shrona.mommytalk.message.domain.type.ReservationStatus;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction(BaseEntity.DEFAULT_CONDITION)
@Table(name = "message_log")
public class MessageLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column
    private ReservationStatus status;

    @Column(length = 1000)
    private String content;

    @Column(name = "reserve_time")
    private LocalDateTime reserveTime;

    @Column(name = "sent_time")
    private LocalDateTime sentTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private MessageType messageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @BatchSize(size = 500)
    @OneToMany(mappedBy = "messageLog", cascade = PERSIST)
    private List<MessageLogLineInfo> messageLogLineInfoList = new ArrayList<>();


    public static MessageLog messageLog(
        Channel channel, MessageType type, Group group, LocalDateTime reserveTime, String content) {
        MessageLog log = new MessageLog();
        log.channel = channel;
        log.group = group;
        log.reserveTime = reserveTime;
        log.messageType = type;
        log.status = ReservationStatus.PREPARE;
        log.content = content;

        return log;
    }

    // 연관관계 메소드
    public void addMessageLogLineInfo(MessageLogLineInfo info) {
        messageLogLineInfoList.add(info);
        info.setMessageLogInfo(this);
    }

    /**
     * 전송 후 메시지 성공 상태로 변경
     */
    public void changeStatusAfterSend() {
        this.status = ReservationStatus.COMPLETE;
        this.sentTime = LocalDateTime.now();
    }

    /**
     * 메시지 전달 취소
     */
    public void cancelStatusBeforeSend() {
        this.status = ReservationStatus.CANCEL;
        this.sentTime = LocalDateTime.now();
    }

    public void updateMessage(String content) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinutesBeforeReservation = reserveTime.minusMinutes(5);

        if (now.isAfter(fiveMinutesBeforeReservation)) {
            throw new LineException(LineErrorCode.EDIT_RESERVED_TIME_EXPIRED);
        }

        this.content = content;
    }

    public void failedStatusBecauseError() {
        this.status = ReservationStatus.FAIL;
    }
}
