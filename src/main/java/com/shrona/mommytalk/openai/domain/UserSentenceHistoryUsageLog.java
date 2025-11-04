package com.shrona.mommytalk.openai.domain;

import com.shrona.mommytalk.common.entity.BaseEntity;
import jakarta.persistence.Column;
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
@Table(name = "user_sentence_history_usage_log")
public class UserSentenceHistoryUsageLog extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String model;           // "gpt-4o"

    @Column
    private Integer promptTokens;   // input 토큰

    @Column
    private Integer completionTokens; // output 토큰

    @Column
    private Integer totalTokens;    // 총 토큰

    @Column
    private String purpose;         // "message_generation", "test"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_prompt_id")
    private MessagePrompt messagePrompt;     // 어느 메시지 타입인지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_sentence_history_id")
    private UserSentenceHistory userSentenceHistory;     // 어느 메시지 타입인지

    public static UserSentenceHistoryUsageLog of(
        UserSentenceHistory userSentenceHistory, MessagePrompt prompt, String model, String purpose,
        Integer promptTokens, Integer completionTokens, Integer totalTokens) {

        UserSentenceHistoryUsageLog userSentenceHistoryUsageLog = new UserSentenceHistoryUsageLog();
        userSentenceHistoryUsageLog.messagePrompt = prompt;
        userSentenceHistoryUsageLog.userSentenceHistory = userSentenceHistory;
        userSentenceHistoryUsageLog.model = model;
        userSentenceHistoryUsageLog.purpose = purpose;
        userSentenceHistoryUsageLog.promptTokens = promptTokens;
        userSentenceHistoryUsageLog.completionTokens = completionTokens;
        userSentenceHistoryUsageLog.totalTokens = totalTokens;

        return userSentenceHistoryUsageLog;
    }


}
