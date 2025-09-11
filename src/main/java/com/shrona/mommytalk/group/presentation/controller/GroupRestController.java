package com.shrona.mommytalk.group.presentation.controller;

import static com.shrona.mommytalk.channel.common.exception.ChannelErrorCode.CHANNEL_NOT_FOUND;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.common.dto.PageResponseDto;
import com.shrona.mommytalk.group.application.GroupService;
import com.shrona.mommytalk.group.domain.Group;
import com.shrona.mommytalk.group.presentation.dtos.response.GroupListResponseDto;
import com.shrona.mommytalk.group.presentation.dtos.response.GroupResponseDto;
import com.shrona.mommytalk.group.presentation.dtos.response.UserGroupMemberResponseDto;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/channels/{channelId}/user-groups")
@RestController
public class GroupRestController {

    private final ChannelService channelService;
    private final GroupService groupService;

    /**
     * 그룹 목록 조회
     */
    @GetMapping
    public ApiResponse<PageResponseDto<GroupListResponseDto>> findUserGroupList(
        @PathVariable("channelId") Long channelId
    ) {

        // 채널 정보 갖고 온다.
        Channel channelInfo = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        Page<Group> groupList = groupService.findGroupList(channelInfo, PageRequest.of(0, 10));

        // Line 유저가 등록된 모든 유저의 숫자를 구한다.
        Map<Long, Integer> groupPlatformUserCount = groupService.findGroupPlatformUserCount(
            groupList.stream().map(Group::getId).toList(), channelInfo.getChannelPlatform());

        // Line이 등록되지 않은 모든 유저의 숫자를 구한다.
        Map<Long, Integer> groupAllUserCount = groupService.findGroupAllUserCount(
            groupList.stream().map(Group::getId).toList(), channelInfo.getChannelPlatform());

        List<GroupListResponseDto> list = groupList.stream().map(
            (groupInfo) -> GroupListResponseDto.of(groupInfo, groupPlatformUserCount,
                groupAllUserCount)).toList();

        return ApiResponse.success(
            PageResponseDto.from(
                list, groupList.getNumber(), groupList.getSize(), groupList.getTotalElements(),
                groupList.getTotalPages()
            )
        );
    }

    /**
     * 그룹 상세 정보 조회
     */
    @GetMapping("/{groupId}")
    public ApiResponse<GroupResponseDto> findUserGroupDetail(
        @PathVariable("channelId") Long channelId,
        @PathVariable("groupId") Long groupId
    ) {

        Group groupInfo = groupService.findGroupById(groupId, true);

        List<UserGroupMemberResponseDto> list = groupInfo.getUserGroupList().stream()
            .map((ug) -> UserGroupMemberResponseDto.of(ug.getUser(), ug)).toList();

        return ApiResponse.success(GroupResponseDto.of(groupInfo, list));
    }

    /**
     * 커스텀 그룹 생성
     */
    @PostMapping
    public void createCustomGroup() {

    }

    @DeleteMapping("/{groupId}")
    public void delUserFromGroup() {

    }

}
