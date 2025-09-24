package com.shrona.mommytalk.user.presentation.controller;

import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.common.dto.PageResponseDto;
import com.shrona.mommytalk.user.application.UserService;
import com.shrona.mommytalk.user.infrastructure.repository.dao.UserListProjection;
import com.shrona.mommytalk.user.presentation.dtos.request.UpdateUserRequestDto;
import com.shrona.mommytalk.user.presentation.dtos.response.UserListResponseDto;
import com.shrona.mommytalk.user.presentation.dtos.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/channels/{channelId}/users")
@RestController
public class UserRestController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<PageResponseDto<UserListResponseDto>> findUserList(
        @RequestParam(required = false, defaultValue = "10") Integer size,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "DATE") String sort,
        @RequestParam(defaultValue = "ASC") String direction,
        @PathVariable Long channelId
    ) {

        Sort sortInfo =
            direction.equalsIgnoreCase("DESC") ?
                Sort.by(Order.desc("createdAt"))
                : Sort.by(Order.asc("createdAt"));
        PageRequest pageRequest = PageRequest.of(page, size, sortInfo);

        Page<UserListProjection> userLists = userService.findUserListByChannelInfoWithPaging(
            channelId, pageRequest);

        return ApiResponse.success(
            PageResponseDto.from(
                userLists.stream().map(UserListResponseDto::from).toList(),
                userLists.getTotalPages(),
                userLists.getNumber(),
                userLists.getTotalElements(),
                userLists.getTotalPages())
        );
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponseDto> findUserById(
        @PathVariable Long userId
    ) {
        UserResponseDto userInfoById = userService.findUserInfoById(userId);
        return ApiResponse.success(userInfoById);
    }

    @PutMapping("/{userId}")
    public ApiResponse<String> updateUserInfo(
        @PathVariable Long userId,
        @RequestBody UpdateUserRequestDto requestDto
    ) {

        // 유저 업데이트
        userService.updateUserInfoByRequest(userId, requestDto);

        return ApiResponse.success("success");
    }

    @PatchMapping("/{userId}/entitlements")
    public ResponseEntity<?> yahoo() {

        return ResponseEntity.ok("");
    }

}
