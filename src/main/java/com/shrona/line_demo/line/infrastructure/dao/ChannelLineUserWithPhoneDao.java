package com.shrona.line_demo.line.infrastructure.dao;

import com.shrona.line_demo.user.domain.vo.PhoneNumber;
import java.time.LocalDateTime;

public record ChannelLineUserWithPhoneDao(
    Long channelUserSeq,
    Long lineSeq,
    String lineId,
    PhoneNumber phoneNumber,
    LocalDateTime createdAt
) {

}
