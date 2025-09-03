package com.shrona.mommytalk.line.domain;

import com.shrona.mommytalk.common.entity.BaseEntity;
import com.shrona.mommytalk.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "line_user")
public class LineUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "line_id", unique = true)
    private String lineId;

    @OneToOne(mappedBy = "lineUser", fetch = FetchType.LAZY)
    private User user;

    @OneToMany(mappedBy = "lineUser")
    private List<ChannelLineUser> channelLineUserList = new ArrayList<>();


    public static LineUser createLineUser(String lineId) {
        LineUser lineUser = new LineUser();

        lineUser.lineId = lineId;

        return lineUser;
    }
}
