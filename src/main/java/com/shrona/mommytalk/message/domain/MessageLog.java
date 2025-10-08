package com.shrona.mommytalk.message.domain;

import static jakarta.persistence.CascadeType.PERSIST;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.entity.BaseEntity;
import com.shrona.mommytalk.entitlement.domain.Entitlement;
import com.shrona.mommytalk.line.common.exception.LineErrorCode;
import com.shrona.mommytalk.line.common.exception.LineException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "group_info")
    private String groupInfo;

    @Column(name = "reserve_time")
    private LocalDateTime reserveTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entitlement_id")
    private Entitlement entitlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private MessageType messageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @BatchSize(size = 500)
    @OneToMany(mappedBy = "messageLog", cascade = PERSIST)
    private List<MessageLogDetail> messageLogDetailList = new ArrayList<>();


    public static MessageLog messageLog(
        Channel channel, MessageType type, LocalDateTime reserveTime, String groupInfo) {
        MessageLog log = new MessageLog();
        log.channel = channel;
        log.reserveTime = reserveTime;
        log.messageType = type;
        log.groupInfo = groupInfo;

        return log;
    }

    // 연관관계 메소드
    public void addMessageLogLineInfo(MessageLogDetail info) {
        messageLogDetailList.add(info);
        info.setMessageLogInfo(this);
    }

    /**
     * MessageLog에 해당하는 상품정보 저장
     */
    public void updateMessageEntitlement(Entitlement entitlement) {
        this.entitlement = entitlement;
    }

    public void updateMessage(String groupInfo) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinutesBeforeReservation = reserveTime.minusMinutes(5);

        // 5분전이는 정보를 바꿀 수 없다.
        if (now.isAfter(fiveMinutesBeforeReservation)) {
            throw new LineException(LineErrorCode.EDIT_RESERVED_TIME_EXPIRED);
        }

        this.groupInfo = groupInfo;
    }
}
