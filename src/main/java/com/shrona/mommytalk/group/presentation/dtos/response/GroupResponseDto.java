package com.shrona.mommytalk.group.presentation.dtos.response;

import static lombok.AccessLevel.PRIVATE;

import com.shrona.mommytalk.group.domain.Group;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder(access = PRIVATE)
public record GroupResponseDto(
    Long id,
    String title,
    String type,
    String product,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,

    List<UserGroupMemberResponseDto> members
) {

    public static GroupResponseDto of(Group group, List<UserGroupMemberResponseDto> members) {
        return GroupResponseDto.builder()
            .id(group.getId())
            .title(group.getName())
            .type("타입")
            .product("상품입니다.")
            .createdAt(group.getCreatedAt())
            .updatedAt(group.getUpdatedAt())
            .members(members)
            .build();
    }
    
}
