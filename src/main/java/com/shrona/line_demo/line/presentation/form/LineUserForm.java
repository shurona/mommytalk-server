package com.shrona.line_demo.line.presentation.form;

import com.shrona.line_demo.line.domain.LineUser;
import java.time.LocalDateTime;

public record LineUserForm(
    Long id,
    String lineId,
    String phoneNumber,
    LocalDateTime joinDate
) {

    public static LineUserForm of(LineUser lineUser) {
        String phone =
            lineUser.getPhoneNumber() != null
                ? lineUser.getPhoneNumber().getPhoneNumber() : "";
        return new LineUserForm(
            lineUser.getId(),
            lineUser.getLineId(),
            phone,
            lineUser.getCreatedAt().plusHours(9)); // TODO: 서버는 utc 사용하고 클라이언트에서 반영하도록 변경
    }

}
