package com.shrona.mommytalk.group.presentation.form;

import com.shrona.mommytalk.group.domain.Group;
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

    public static GroupForm of(
        Group group, int friendCount, int userCount) {

        return new GroupForm(
            group.getId(),
            group.getName(),
            group.getDescription(),
            friendCount,
            userCount,
            group.getCreatedAt().plusHours(9), // TODO: 서버는 utc 사용하고 클라이언트에서 반영하도록 변경,
            group.getUpdatedAt().plusHours(9) // TODO: 서버는 utc 사용하고 클라이언트에서 반영하도록 변경
        );
    }

}
