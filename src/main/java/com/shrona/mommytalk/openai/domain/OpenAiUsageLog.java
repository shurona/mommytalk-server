package com.shrona.mommytalk.openai.domain;

import com.shrona.mommytalk.common.entity.BaseEntity;
import com.shrona.mommytalk.message.domain.MessageType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
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
@Table(name = "open_ai_usage_log")
public class OpenAiUsageLog extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String model;           // "gpt-4o"
    private Integer promptTokens;   // input 토큰
    private Integer completionTokens; // output 토큰
    private Integer totalTokens;    // 총 토큰

    private String purpose;         // "message_generation", "test"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_prompt_id")
    private MessagePrompt messagePrompt;     // 어느 메시지 타입인지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_type_id")
    private MessageType messageType;     // 어느 메시지 타입인지

    public static OpenAiUsageLog of(
        MessageType messageType, MessagePrompt prompt, String model, String purpose,
        Integer promptTokens, Integer completionTokens, Integer totalTokens) {

        OpenAiUsageLog openAiUsageLog = new OpenAiUsageLog();
        openAiUsageLog.messagePrompt = prompt;
        openAiUsageLog.messageType = messageType;
        openAiUsageLog.model = model;
        openAiUsageLog.purpose = purpose;
        openAiUsageLog.promptTokens = promptTokens;
        openAiUsageLog.completionTokens = completionTokens;
        openAiUsageLog.totalTokens = totalTokens;

        return openAiUsageLog;
    }

}
