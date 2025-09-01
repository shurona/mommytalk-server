package com.shrona.mommytalk.line.domain;


import com.shrona.mommytalk.common.entity.BaseEntity;
import com.shrona.mommytalk.user.domain.User;
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
@Table(name = "channel_user_connection")
public class ChannelUserConnection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private boolean follow;

    @OneToMany(mappedBy = "channelUserConnection")
    private List<LineMessage> lineMessageList = new ArrayList<>();

    /**
     * 생성 메소드
     */
    public static ChannelUserConnection create(Channel channel, User user) {
        ChannelUserConnection channelUserConnection = new ChannelUserConnection();
        channelUserConnection.user = user;
        channelUserConnection.channel = channel;
        channelUserConnection.follow = true;

        return channelUserConnection;
    }

    public void changeFollowStatus(boolean status) {
        this.follow = status;
    }
}
