package com.shrona.mommytalk.openai.domain;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.entity.BaseEntity;
import com.shrona.mommytalk.openai.domain.type.PromptType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "message_prompt")
public class MessagePrompt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(length = 5000)
    private String prompt;

    @Column
    private String label;

    @Column
    private Boolean selected;

    @Enumerated(EnumType.STRING)
    @Column
    private PromptType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    public static MessagePrompt of(Channel channel, String prompt, String label, PromptType type) {

        MessagePrompt messagePrompt = new MessagePrompt();
        messagePrompt.prompt = prompt;
        messagePrompt.label = label;
        messagePrompt.type = type;
        messagePrompt.channel = channel;
        messagePrompt.selected = false;
        return messagePrompt;
    }

    /**
     * 등록 시 변경사항
     */
    public void updateWhenRegister(String prompt, String label) {
        this.prompt = prompt;
        this.label = label;
    }

    /**
     * 현재 프롬프트 비활성화
     */
    public void disablePrompt() {
        this.selected = false;
    }

    /**
     * 프롬프트를 등록한다.
     */
    public void registerPrompt() {
        this.selected = true;
    }
}