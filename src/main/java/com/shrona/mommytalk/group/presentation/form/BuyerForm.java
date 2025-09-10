package com.shrona.mommytalk.group.presentation.form;

import com.shrona.mommytalk.group.domain.UserGroup;
import com.shrona.mommytalk.user.domain.type.AddUserMethod;
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

        String phoneNumber = userGroup.getUser().getPhoneNumber() == null ? null
            : userGroup.getUser().getPhoneNumber().getPhoneNumber();

        String lineId = userGroup.getUser().getLineUser() != null ?
            userGroup.getUser().getLineUser().getLineId() : null;

        return new BuyerForm(
            userGroup.getId(),
            phoneNumber,
            lineId,
            addMethod,
            userGroup.getCreatedAt().plusHours(9), // TODO: 서버는 utc 사용하고 클라이언트에서 반영하도록 변경,
            userGroup.getUpdatedAt().plusHours(9) // TODO: 서버는 utc 사용하고 클라이언트에서 반영하도록 변경
        );
    }

}
