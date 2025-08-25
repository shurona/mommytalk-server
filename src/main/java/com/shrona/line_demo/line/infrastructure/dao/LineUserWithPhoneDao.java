package com.shrona.line_demo.line.infrastructure.dao;

import com.shrona.line_demo.user.domain.vo.PhoneNumber;

public record LineUserWithPhoneDao(
    Long id,
    String lineId,
    PhoneNumber phoneNumber
) {

}
