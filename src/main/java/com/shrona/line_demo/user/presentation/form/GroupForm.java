package com.shrona.line_demo.user.presentation.form;

import com.shrona.line_demo.user.domain.Group;
import java.time.LocalDateTime;

public record GroupForm(
    Long id,
    String name,
    String description,
    Integer friendCount,
    Integer totalCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static GroupForm of(Group group) {

        return new GroupForm(
            group.getId(),
            group.getName(),
            group.getDescription(),
            0, 0, // todo: 이거 로직 추가합시다.
            group.getCreatedAt(),
            group.getUpdatedAt()
        );
    }

}
