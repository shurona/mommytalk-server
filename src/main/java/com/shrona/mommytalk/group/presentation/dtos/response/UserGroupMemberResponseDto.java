package com.shrona.mommytalk.group.presentation.dtos.response;

import static lombok.AccessLevel.PRIVATE;

import com.shrona.mommytalk.group.domain.UserGroup;
import com.shrona.mommytalk.user.domain.User;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder(access = PRIVATE)
public record UserGroupMemberResponseDto(
    Long userId,
    String phoneNumber,
    String name,
    Boolean isFriend,
    LocalDateTime registeredAt
) {

    public static UserGroupMemberResponseDto of(User user, UserGroup userGroup) {
        String phoneInfo = user.getPhoneNumber() != null
            ? user.getPhoneNumber().getPhoneNumber() : null;

        return UserGroupMemberResponseDto.builder()
            .userId(user.getId())
            .phoneNumber(phoneInfo)
            .name(user.getName())
            .isFriend(user.getLineUser() != null)
            .registeredAt(userGroup.getCreatedAt().plusHours(9)) // TODO
            .build();
    }

}
