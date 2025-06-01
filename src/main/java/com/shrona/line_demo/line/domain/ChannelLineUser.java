package com.shrona.line_demo.line.domain;


import com.shrona.line_demo.common.entity.BaseEntity;
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
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction(BaseEntity.DEFAULT_CONDITION)
@Table(name = "channel_line_user")
public class ChannelLineUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_user_id")
    private LineUser lineUser;

    @Column
    private boolean follow;

    @OneToMany(mappedBy = "channelLineUser")
    private List<LineMessage> lineMessageList = new ArrayList<>();

    /**
     * 생성 메소드
     */
    public static ChannelLineUser create(Channel channel, LineUser lineUser) {
        ChannelLineUser channelLineUser = new ChannelLineUser();
        channelLineUser.lineUser = lineUser;
        channelLineUser.channel = channel;
        channelLineUser.follow = true;

        return channelLineUser;
    }

    public void changeFollowStatus(boolean status) {
        this.follow = status;
    }
}
