package com.shrona.mommytalk.line.domain;

import com.shrona.mommytalk.common.entity.BaseEntity;
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
@Table(name = "line_message")
public class LineMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_line_user_id")
    private ChannelLineUser channelLineUser;

    /**
     * 생성 메소드
     */
    public static LineMessage createLineMessage(ChannelLineUser channelLineUser, String content) {
        LineMessage lineMessage = new LineMessage();

        lineMessage.channelLineUser = channelLineUser;
        lineMessage.content = content;

        return lineMessage;
    }

}
