package com.shrona.mommytalk.line.infrastructure.dao;

import com.shrona.mommytalk.user.domain.vo.PhoneNumber;
import java.time.LocalDateTime;

public record ChannelLineUserWithPhoneDao(
    Long channelUserSeq,
    Long userSeq,
    String lineId,
    PhoneNumber phoneNumber,
    LocalDateTime createdAt
) {

}