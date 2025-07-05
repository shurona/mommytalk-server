package com.shrona.line_demo.line.domain;

import com.shrona.line_demo.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction(BaseEntity.DEFAULT_CONDITION)
@Table(name = "channel_info")
public class Channel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column
    private String description;

    @Column(name = "channel_id", unique = true)
    private String channelId; // 채널 정보

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "invite_message", length = 1000)
    private String inviteMessage;

    public static Channel createChannel(String name, String description) {
        Channel channel = new Channel();
        channel.name = name;
        channel.description = description;
        return channel;
    }

    public void updateInviteMessage(String message) {
        this.inviteMessage = message;
    }

}
