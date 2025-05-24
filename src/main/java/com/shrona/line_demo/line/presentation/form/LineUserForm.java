package com.shrona.line_demo.line.presentation.form;

import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import java.time.LocalDateTime;

public record LineUserForm(
    String lineId,
    String phoneNumber,
    LocalDateTime joinDate
) {

    public static LineUserForm of(String lineId, PhoneNumber phoneNumber, LocalDateTime joinDate) {
        return new LineUserForm(lineId, phoneNumber.getPhoneNumber(), joinDate);
    }

}
