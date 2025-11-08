package com.shrona.mommytalk.message.domain;

import com.shrona.mommytalk.common.entity.BaseEntity;
import com.shrona.mommytalk.elevenlabs.domain.ElevenLabsMedia;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_one_link")
    private ElevenLabsMedia headerOneLink; // 엄마 발음

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_two_link")
    private ElevenLabsMedia headerTwoLink; // 아이 발음

    @Column(name = "mommy_voca")
    private String mommyVoca;

    @Column(name = "diary_url")
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
    public static MessageContent createByAi(
        MessageType type, String content, int childLevel, int userLevel) {
        return MessageContent.builder()
            .content(content)
            .childLevel(childLevel)
            .userLevel(userLevel)
            .messageType(type)
            .approved(false)
            .build();
    }

    /**
     * Mock URL을 포함한 메시지 컨텐츠 생성
     */
    public static MessageContent ofWithMockUrlsForUpsert(
        MessageType type, String content, String diaryUrl, int childLevel, int userLevel) {
        return MessageContent.builder()
            .content(content)
            .childLevel(childLevel)
            .userLevel(userLevel)
            .messageType(type)
            .diaryUrl(diaryUrl)
            .approved(false)
            .build();
    }

    /**
     * 컨텐츠 업데이트 및 승인
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

    /**
     * 컨텐츠 승인 취소
     */
    public void cancelApproval() {
        this.approved = false;
    }

    /**
     * User와 레벨정보를 매핑하기 위한 키 프로퍼티 생성 userLevel_childLevel
     */
    public String createKeyPropertyForMessageContent() {
        return this.userLevel + "_" + this.childLevel;
    }

    /**
     * 미디어 버튼 생성
     */
    public void updateButtonOne(ElevenLabsMedia media) {
        this.headerOneLink = media;
    }

    /**
     * 미디어 버튼 생성
     */
    public void updateButtonTwo(ElevenLabsMedia media) {
        this.headerTwoLink = media;
    }

    /**
     * 승인 되는 조건을 확인한다.
     */
    public boolean checkApprovedCondition() {

//        return true;

        if (childLevel == 1) {
            // 아이 레벨 1이면 아이 링크는 패스
            return this.content != null && this.getHeaderOneLink() != null
                && this.getDiaryUrl() != null;
        } else {
            // 4개의 데이터 중 하나라도 비어있으면 승인 불가
            return this.content != null && this.getHeaderOneLink() != null
                && this.getHeaderTwoLink() != null && this.getDiaryUrl() != null;
        }
    }
}