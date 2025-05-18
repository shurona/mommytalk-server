package com.shrona.line_demo.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "admin_user")
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String loginId;

    @Column
    private String password;

    @Column(unique = true)
    private String lineId;

    @Column
    private String description;

    public static AdminUser createAdminUser(String loginId, String password, String lineId) {
        AdminUser adminUser = new AdminUser();

        adminUser.loginId = loginId;
        adminUser.password = password;
        adminUser.lineId = lineId;

        return adminUser;
    }

}
