package com.shrona.line_demo.user.domain;


import com.shrona.line_demo.common.entity.BaseEntity;
import com.shrona.line_demo.line.domain.LineUser;
import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "_user")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    @Column(name = "phone_number", unique = true)
    private PhoneNumber phoneNumber;

    @Column(name = "line_id", unique = true)
    private String lineId;

    @Column
    private String description;

    @OneToMany(mappedBy = "user")
    private List<UserGroup> userGroupList = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_user_id")
    private LineUser lineUser;


    public static User createUser(PhoneNumber phoneNumber) {
        User user = new User();
        user.phoneNumber = phoneNumber;

        return user;
    }

    public static User createUserWithLine(PhoneNumber phoneNumber, LineUser lineUser) {
        User user = new User();
        user.phoneNumber = phoneNumber;
        user.lineUser = lineUser;
        user.lineId = lineUser.getLineId();

        return user;
    }

}
