package com.shrona.line_demo.admin.domain;

import com.shrona.line_demo.common.entity.BaseEntity;
import com.shrona.line_demo.line.domain.Channel;
import com.shrona.line_demo.line.domain.LineUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "test_user")
public class TestUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_user_id")
    private LineUser lineUser;

    public static TestUser createTestUser(Channel channel, LineUser lineUser, String description) {
        TestUser testUser = new TestUser();
        testUser.channel = channel;
        testUser.lineUser = lineUser;
        testUser.description = description;

        return testUser;
    }
}
