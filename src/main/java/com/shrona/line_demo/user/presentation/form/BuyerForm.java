package com.shrona.line_demo.user.presentation.form;

import com.shrona.line_demo.user.domain.UserGroup;
import com.shrona.line_demo.user.domain.type.AddUserMethod;
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

        String addMethod = userGroup.getUser().getAddMethod().equals(AddUserMethod.LINE)
            ? "라인으로 추가" : "전화번호로 추가";

        return new BuyerForm(
            userGroup.getId(),
            userGroup.getUser().getPhoneNumber().getPhoneNumber(),
            userGroup.getUser().getLineId(),
            addMethod,
            userGroup.getCreatedAt(),
            userGroup.getUpdatedAt()
        );
    }

}
