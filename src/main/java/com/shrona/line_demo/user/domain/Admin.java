package com.shrona.line_demo.user.domain;

import com.shrona.line_demo.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, name = "username")
    private String loginId;

    @Column
    private String description;

    @Column
    private String password;


}
