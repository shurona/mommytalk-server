package com.shrona.mommytalk.user.domain;

import com.shrona.mommytalk.common.entity.BaseEntity;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@SQLRestriction(BaseEntity.DEFAULT_CONDITION)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_group")
public class UserGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    public static UserGroup createUserGroup(User user, Group group) {
        UserGroup userGroup = new UserGroup();
        userGroup.user = user;
        userGroup.group = group;
        return userGroup;
    }

    public void changeUser(User user) {
        this.user = user;
    }

    public void deleteUserGroup() {
        this.isDeleted = true;
    }
}
