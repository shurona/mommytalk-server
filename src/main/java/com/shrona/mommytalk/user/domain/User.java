package com.shrona.mommytalk.user.domain;


import com.shrona.mommytalk.common.entity.BaseEntity;
import com.shrona.mommytalk.group.domain.UserGroup;
import com.shrona.mommytalk.kakao.domain.KakaoUser;
import com.shrona.mommytalk.line.domain.LineUser;
import com.shrona.mommytalk.user.domain.type.AddUserMethod;
import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction(BaseEntity.DEFAULT_CONDITION)
@Table(name = "_user")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    @Column(name = "phone_number", unique = true)
    private PhoneNumber phoneNumber;

    @Column
    private String description;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private Integer userLevel;

    @Column
    private String childName;

    @Column
    private Integer childLevel;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "add_method")
    private AddUserMethod addMethod;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_user_id")
    private LineUser lineUser;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kakao_user_id")
    private KakaoUser kakaoUser;

    @OneToMany(mappedBy = "user")
    private List<UserGroup> userGroupList = new ArrayList<>();

    /**
     * 라인 없이 신규 유저 추가
     */
    public static User createUser(PhoneNumber phoneNumber) {
        User user = new User();
        user.phoneNumber = phoneNumber;
        user.addMethod = AddUserMethod.PHONE_NUMBER;

        return user;
    }

    /**
     * 라인이 이미 존재할 때 신규 유저 추가
     */
    public static User createUserWithLine(PhoneNumber phoneNumber, LineUser lineUser) {
        User user = new User();
        user.phoneNumber = phoneNumber;
        user.lineUser = lineUser;
        user.addMethod = AddUserMethod.LINE;

        return user;
    }

    /**
     * 존재하는 유저에게 라인 정보를 넣어준다.
     */
    public void matchUserWithLine(LineUser lineUser) {
        this.lineUser = lineUser;
    }

    public void clearLineUserAndPhoneNumber() {
        this.lineUser = null;
        this.phoneNumber = null;
    }

    public void deleteUser() {
        this.isDeleted = true;
    }

    /**
     * User의 휴대전화 정보를 변경해준다.
     */
    public void updatePhoneNumber(PhoneNumber phone) {
        // null이 아닌 경우에만 바꿔준다.
        if (phone != null) {
            this.phoneNumber = phone;
        }
    }

    /**
     * 휴대전화 있는 지 확인
     */
    public boolean hasPhoneNumber() {
        return phoneNumber != null;
    }

    /**
     * 라인 유저 있는 지 확인
     */
    public boolean hasLineUser() {
        return lineUser != null;
    }


    /**
     * User에 휴대전화와 LineUser를 동시에 등록해준다.
     */
    public void updateLineAndPhoneNumber(LineUser lineUser, PhoneNumber phone) {
        this.lineUser = lineUser;
        this.phoneNumber = phone;
    }

    public void updateUserFromRequest(String childName, Integer childLevel, Integer userLevel) {
        this.childName = childName;
        this.childLevel = childLevel;
        this.userLevel = userLevel;
    }

    /**
     * MessageContent와 레벨정보를 매핑하기 위한 키 프로퍼티 생성 userLevel_childLevel
     */
    public String createKeyPropertyForMessageContent() {
        return this.userLevel + "_" + this.childLevel;
    }

}
