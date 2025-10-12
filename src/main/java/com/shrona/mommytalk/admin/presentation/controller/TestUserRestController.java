package com.shrona.mommytalk.admin.presentation.controller;


import com.shrona.mommytalk.admin.application.AdminService;
import com.shrona.mommytalk.admin.presentation.dtos.TestUserAddRequestDto;
import com.shrona.mommytalk.admin.presentation.dtos.TestUserResponseDto;
import com.shrona.mommytalk.admin.presentation.form.TestUserServiceDto;
import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelErrorCode;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.user.application.UserService;
import com.shrona.mommytalk.user.domain.User;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@RequestMapping("/api/v1/channels/{channelId}/test-users")
@RestController
public class TestUserRestController {

    private final AdminService adminService;
    private final UserService userService;
    private final ChannelService channelService;

    @GetMapping
    public ApiResponse<List<TestUserResponseDto>> findTestUserList(
        @PathVariable("channelId") Long channelId
    ) {
        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(ChannelErrorCode.CHANNEL_NOT_FOUND));

        List<TestUserServiceDto> allTestUser = adminService.findAllTestUser(channel);

        return ApiResponse.success(
            allTestUser.stream().map(
                TestUserResponseDto::of
            ).toList()
        );


    }

    @PostMapping
    public ApiResponse<Boolean> registerTestUser(
        @PathVariable("channelId") Long channelId,
        @RequestBody TestUserAddRequestDto requestBody
    ) {

        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(ChannelErrorCode.CHANNEL_NOT_FOUND));

        // 휴대전화를 기준으로 라인 아이디를 갖고 온다.
        User userByPhoneNumber = userService.findUserByPhoneNumber(
            requestBody.phoneNumber());

        if (Objects.isNull(userByPhoneNumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "없는 번호 입니다.");
        }

        adminService.registerTestNumber(channel, userByPhoneNumber);

        return ApiResponse.success(true);
    }

    @DeleteMapping("/{testUserId}")
    public ApiResponse<Boolean> deleteTestUser(
        @PathVariable("channelId") Long channelId,
        @PathVariable("testUserId") Long testUserId
    ) {

        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(ChannelErrorCode.CHANNEL_NOT_FOUND));

        adminService.deleteTestUser(channel, testUserId);

        return ApiResponse.success(true);
    }


}
