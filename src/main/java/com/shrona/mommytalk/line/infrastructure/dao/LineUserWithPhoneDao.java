package com.shrona.mommytalk.line.infrastructure.dao;

import com.shrona.mommytalk.user.domain.vo.PhoneNumber;

public record LineUserWithPhoneDao(
    Long id,
    String lineId,
    PhoneNumber phoneNumber
) {

}
