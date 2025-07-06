package com.shrona.line_demo.line.domain;

import com.shrona.line_demo.common.entity.BaseEntity;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @Embedded
    private PhoneNumber phoneNumber;

    @OneToMany(mappedBy = "lineUser")
    private List<ChannelLineUser> channelLineUserList = new ArrayList<>();

    public static LineUser createLineUser(String lineId) {
        LineUser lineUser = new LineUser();

        lineUser.lineId = lineId;

        return lineUser;
    }

    public void clearPhoneNumber() {
        this.phoneNumber = null;
    }

    public void settingPhoneNumber(PhoneNumber phone) {
        // null이 아닌 경우에만 바꿔준다.
        if (phone != null) {
            this.phoneNumber = phone;
        }
    }
}
