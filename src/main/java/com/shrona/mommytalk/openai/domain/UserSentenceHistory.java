package com.shrona.mommytalk.openai.domain;

import com.shrona.mommytalk.common.entity.BaseEntity;
import com.shrona.mommytalk.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_sentence_history")
public class UserSentenceHistory extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String sentence;

    @Column(length = 1000)
    private String output;

    @Column(name = "generate_date")
    private LocalDate generateDate;

    public static UserSentenceHistory of(User user, String sentence) {

        UserSentenceHistory userSentenceHistory = new UserSentenceHistory();

        userSentenceHistory.user = user;
        userSentenceHistory.sentence = sentence;
        userSentenceHistory.generateDate = LocalDate.now(ZoneId.of("Asia/Seoul"));

        return userSentenceHistory;
    }

    public void updateOutput(String output) {
        this.output = output;
    }

    public boolean isPast() {

        return generateDate.plusDays(1).isAfter(LocalDate.now());
    }


}
