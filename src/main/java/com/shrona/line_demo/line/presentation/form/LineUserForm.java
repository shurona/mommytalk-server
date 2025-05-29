package com.shrona.line_demo.line.presentation.form;

import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import java.time.LocalDateTime;

public record LineUserForm(
    String lineId,
    String phoneNumber,
    LocalDateTime joinDate
) {

    public static LineUserForm of(String lineId, PhoneNumber phoneNumber, LocalDateTime joinDate) {
        String phone = phoneNumber != null ? phoneNumber.getPhoneNumber() : "";
        return new LineUserForm(lineId,
            phone,
            joinDate.plusHours(9)); // TODO: 서버는 utc 사용하고 클라이언트에서 반영하도록 변경
    }

}
