package com.shrona.line_demo.user.presentation.form;

import com.shrona.line_demo.user.domain.UserGroup;
import java.time.LocalDateTime;

public record BuyerForm(
    Long id,
    String phone,
    String lineId,
    String addInfo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static BuyerForm of(UserGroup userGroup) {
        return new BuyerForm(
            userGroup.getId(),
            userGroup.getUser().getPhoneNumber().getPhoneNumber(),
            userGroup.getUser().getLineId(),
            "뭘까요",
            userGroup.getCreatedAt(),
            userGroup.getUpdatedAt()
        );
    }

}
