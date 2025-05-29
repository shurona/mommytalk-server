package com.shrona.line_demo.user.presentation.form;

import com.shrona.line_demo.user.domain.Group;
import com.shrona.line_demo.user.domain.UserGroup;
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

        int friendCount = 0;
        for (UserGroup userGroup : group.getUserGroupList()) {
            if (userGroup.getUser().getLineId() != null &&
                !userGroup.getUser().getLineId().isEmpty()) {
                friendCount += 1;
            }
        }

        return new GroupForm(
            group.getId(),
            group.getName(),
            group.getDescription(),
            friendCount, group.getUserGroupList().size(),
            group.getCreatedAt().plusHours(9), // TODO: 서버는 utc 사용하고 클라이언트에서 반영하도록 변경,
            group.getUpdatedAt().plusHours(9) // TODO: 서버는 utc 사용하고 클라이언트에서 반영하도록 변경
        );
    }

}
