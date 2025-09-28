package com.shrona.mommytalk.message.domain;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder(access = AccessLevel.PRIVATE)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "message_content")
public class MessageContent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 1000)
    private String content;

    @Column
    private Integer childLevel;

    @Column
    private Integer userLevel;

    @Column
    private String headerOneLink;

    @Column
    private String headerTwoLink;

    @Column
    private String mommyVoca;

    @Column
    private String diaryUrl;

    @Column
    private Boolean approved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_type_id")
    private MessageType messageType;

    /**
     * 처음 생성 할 때 메시지 컨텐츠
     */
    public static MessageContent of(
        MessageType type, String content, int childLevel, int userLevel) {
        return MessageContent.builder()
            .content(content)
            .childLevel(childLevel)
            .userLevel(userLevel)
            .messageType(type)
            .approved(false) // 처음 생성 시에는 false로 한다
            .build();
    }

    /**
     * Mock URL을 포함한 메시지 컨텐츠 생성
     */
    public static MessageContent ofWithMockUrls(
        MessageType type, String content, int childLevel, int userLevel) {
        return MessageContent.builder()
            .content(content)
            .childLevel(childLevel)
            .userLevel(userLevel)
            .messageType(type)
            .headerOneLink("https://cdn.example.com/mock-mom-audio.mp3")
            .headerTwoLink("https://cdn.example.com/mock-child-audio.mp3")
            .diaryUrl("https://mamitalk.example.com/diary")
            .approved(false)
            .build();
    }

    /**
     * 컨텐츠 업데이트
     */
    public void updateContent(String newContent, String newDiaryUrl) {
        this.content = newContent;
        this.diaryUrl = newDiaryUrl;
    }

    /**
     * 컨텐츠 승인 (이미 승인된 경우 업데이트 안함)
     */
    public boolean approve() {
        if (Boolean.TRUE.equals(this.approved)) {
            return false; // 이미 승인됨, 업데이트 불필요
        }
        this.approved = true;
        return true; // 승인 완료, 업데이트 필요
    }

}