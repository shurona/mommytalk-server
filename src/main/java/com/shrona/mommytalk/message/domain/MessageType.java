package com.shrona.mommytalk.message.domain;


import com.shrona.mommytalk.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private String title;

    @Column
    private String text;

    @Column
    private LocalDate deliveryTime;

    @OneToMany(mappedBy = "messageType")
    private List<MessageTemplate> messageTemplateList = new ArrayList<>();

    public static MessageType of(String title, String text, LocalDate deliveryTime) {
        MessageType type = new MessageType();
        type.title = title;
        type.text = text;
        type.deliveryTime = deliveryTime;
        return type;
    }

}
