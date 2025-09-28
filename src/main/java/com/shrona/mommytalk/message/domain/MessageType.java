package com.shrona.mommytalk.message.domain;


import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.entity.BaseEntity;
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
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "message_type")
public class MessageType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String theme;

    @Column
    private String context;

    @Column
    private LocalDate deliveryTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @OneToMany(mappedBy = "messageType")
    private List<MessageContent> messageContentList = new ArrayList<>();

    public static MessageType of(String theme, String context, LocalDate deliveryTime, Channel channel) {
        MessageType type = new MessageType();
        type.theme = theme;
        type.context = context;
        type.deliveryTime = deliveryTime;
        type.channel = channel;
        return type;
    }

    public void updateContent(String theme, String context) {
        this.theme = theme;
        this.context = context;
    }

}
