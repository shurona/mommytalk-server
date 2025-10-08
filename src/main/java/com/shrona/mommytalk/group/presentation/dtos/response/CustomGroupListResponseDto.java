package com.shrona.mommytalk.group.presentation.dtos.response;

import static lombok.AccessLevel.PRIVATE;

import com.shrona.mommytalk.group.domain.Group;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.Builder;

@Builder(access = PRIVATE)
public record CustomGroupListResponseDto(
    Long id,
    String title,
    String type,
    Integer memberCount,
    Integer friendCount,
    String createdAt,
    String updatedAt
) {

    public static CustomGroupListResponseDto of(
        Group groupInfo, Map<Long, Integer> groupPlatformUserCount,
        Map<Long, Integer> groupAllUserCount
    ) {

        return CustomGroupListResponseDto.builder()
            .id(groupInfo.getId())
            .title(groupInfo.getName())
            .type(groupInfo.getGroupType().getCode())
            .memberCount(groupAllUserCount.getOrDefault(groupInfo.getId(), 0))
            .friendCount(groupPlatformUserCount.getOrDefault(groupInfo.getId(), 0))
            .createdAt(groupInfo.getCreatedAt().plusHours(9)
                .format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm")))
            .updatedAt(groupInfo.getUpdatedAt().plusHours(9).format(DateTimeFormatter.ofPattern(
                "yyyy.MM.dd. HH:mm"))) // TODO: 서버는 utc 사용하고 클라이언트에서 반영하도록 변경
            .build();
    }

}
