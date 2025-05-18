package com.shrona.line_demo.line.domain;

import com.shrona.line_demo.common.entity.BaseEntity;
import com.shrona.line_demo.line.domain.type.ReservationStatus;
import com.shrona.line_demo.user.domain.Group;
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
@Table(name = "message_log")
public class MessageLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column
    private ReservationStatus status;

    @Column
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

    public static MessageLog messageLog(
        MessageType type, Group group, LocalDateTime reserveTime, String content) {
        MessageLog log = new MessageLog();
        log.group = group;
        log.reserveTime = reserveTime;
        log.messageType = type;
        log.status = ReservationStatus.PREPARE;
        log.content = content;
        return log;
    }

    public void changeStatusAfterSend() {
        this.status = ReservationStatus.COMPLETE;
        this.sentTime = LocalDateTime.now();
    }

}
