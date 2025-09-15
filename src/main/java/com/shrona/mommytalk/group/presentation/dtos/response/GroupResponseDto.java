package com.shrona.mommytalk.group.presentation.dtos.response;

import static lombok.AccessLevel.PRIVATE;

import com.shrona.mommytalk.group.domain.Group;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Builder;

@Builder(access = PRIVATE)
public record GroupResponseDto(
    Long id,
    String title,
    String type,
    String product,
    String createdAt,
    String updatedAt,

    List<UserGroupMemberResponseDto> members
) {

    public static GroupResponseDto of(Group group, List<UserGroupMemberResponseDto> members) {
        return GroupResponseDto.builder()
            .id(group.getId())
            .title(group.getName())
            .type(group.getGroupType().getCode())
            .product("상품입니다.")
            .createdAt(
                group.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm")))
            .updatedAt(
                group.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm")))
            .members(members)
            .build();
    }

}
