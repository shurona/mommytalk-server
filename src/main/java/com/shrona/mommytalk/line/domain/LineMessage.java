package com.shrona.mommytalk.line.domain;

import com.shrona.mommytalk.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column
    private Long lineUserId;

    @Column(name = "channel_id")
    private Long channelId;

    /**
     * 생성 메소드
     */
    public static LineMessage createLineMessage(
        Long channelId, Long lineUserId, String content) {
        LineMessage lineMessage = new LineMessage();

        lineMessage.channelId = channelId;
        lineMessage.lineUserId = lineUserId;
        lineMessage.content = content;

        return lineMessage;
    }

}
