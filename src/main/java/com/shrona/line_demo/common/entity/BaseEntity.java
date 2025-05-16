package com.shrona.line_demo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

    public static final String DEFAULT_CONDITION = "is_deleted = false";

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    protected LocalDateTime createdAt;


    @Column(name = "updated_at")
    @LastModifiedDate
    protected LocalDateTime updatedAt;


    @Column(name = "is_deleted")
    protected Boolean isDeleted = false;


}

