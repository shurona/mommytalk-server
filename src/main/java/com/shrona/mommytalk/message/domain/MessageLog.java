package com.shrona.mommytalk.message.domain;

import static jakarta.persistence.CascadeType.PERSIST;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.channel.domain.ChannelPlatform;
import com.shrona.mommytalk.common.entity.BaseEntity;
import com.shrona.mommytalk.common.utils.DateTimeUtils;
import com.shrona.mommytalk.entitlement.domain.Entitlement;
import com.shrona.mommytalk.group.domain.Group;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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

    @Column(name = "include_custom_group_ids")
    private String includeCustomGroupIds;  // "1,2,3"

    @Column(name = "except_group_ids")
    private String exceptGroupIds;  // "1,2,3"

    @Column(name = "reserve_time")
    private LocalDateTime reserveTime;

    @Column
    private Boolean cancel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entitlement_group_id")
    private Group entitlementGroup;

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
        log.cancel = false;
        return log;
    }

    // 연관관계 메소드
    public void addMessageLogDetailInfo(MessageLogDetail info) {
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

    /**
     * MessageLog를 cancel 한다.
     */
    public void cancelMessageLog() {
        this.cancel = true;
    }

    public void updateGroupInfo(Group entitlementGroup, List<Long> includeIds,
        List<Long> exceptIds) {
        this.entitlementGroup = entitlementGroup;

        if (includeIds != null && !includeIds.isEmpty()) {
            this.includeCustomGroupIds = includeIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        }

        if (exceptIds != null && !exceptIds.isEmpty()) {
            this.exceptGroupIds = exceptIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        }
    }

    /**
     * 포함 그룹 아이디 목록을 갖고 온다.
     */
    public List<Long> getIncludeCustomGroupIdsAsList() {
        if (includeCustomGroupIds == null || includeCustomGroupIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(includeCustomGroupIds.split(","))
            .map(Long::parseLong)
            .toList();
    }

    /**
     * 제외 그룹 아이디 목록을 갖고 온다.
     */
    public List<Long> getExceptGroupIdsAsList() {
        if (exceptGroupIds == null || exceptGroupIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(exceptGroupIds.split(","))
            .map(Long::parseLong)
            .toList();
    }

    /**
     * 신규 MessageLogDetail을 추가할 수 잇는 지 확인한다.
     * 카카오는 reserveTime을 발송일(KST) 의미로 쓰므로 그날이 끝나기 전까지 추가 가능하고,
     * 라인은 reserveTime이 실제 발송 시각이므로 그 전까지만 추가 가능하다.
     */
    public boolean canAddNewDetails() {
        if (this.cancel) {
            return false;
        }
        if (this.channel.getChannelPlatform() == ChannelPlatform.KAKAO) {
            LocalDate sendDateKst = DateTimeUtils.toKst(this.reserveTime).toLocalDate();
            return !DateTimeUtils.todayKst().isAfter(sendDateKst);
        }
        return this.reserveTime.isAfter(LocalDateTime.now());
    }
}
