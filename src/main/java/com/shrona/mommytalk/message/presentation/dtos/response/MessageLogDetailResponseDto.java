package com.shrona.mommytalk.message.presentation.dtos.response;

import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.channel.domain.ChannelPlatform;
import com.shrona.mommytalk.message.domain.MessageLogDetail;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record MessageLogDetailResponseDto(
    Long id,
    String snsId,
    Integer userLevel,
    Integer childLevel,
    String sendStatus,
    LocalDateTime sentAt
) {

    public static MessageLogDetailResponseDto of(Channel channel, MessageLogDetail logDetail) {

        String snsId = channel.getChannelPlatform().equals(ChannelPlatform.LINE)
            ? logDetail.getUser().getLineUser().getLineId()
            : logDetail.getUser().getPhoneNumber().getPhoneNumber();

        return MessageLogDetailResponseDto
            .builder()
            .id(logDetail.getId())
            .snsId(snsId)
            .childLevel(logDetail.getUser().getChildLevel())
            .userLevel(logDetail.getUser().getUserLevel())
            .sendStatus(logDetail.getStatus().getStatus())
            .sentAt(logDetail.getSentTime())
            .build();
    }

}
