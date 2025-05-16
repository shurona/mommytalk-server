package com.shrona.line_demo.user.domain;

import com.shrona.line_demo.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
@Table(name = "custom_group")
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<UserGroup> userGroupList = new ArrayList<>();

    public static Group createGroup(String name, String description) {
        Group group = new Group();
        group.name = name;
        group.description = description;

        return group;
    }

    public void addUserToGroup(List<User> userList) {
        for (User user : userList) {
            userGroupList.add(UserGroup.createUserGroup(user, this));
        }
    }

    public void deleteGroup() {
        this.isDeleted = false;
    }
}
