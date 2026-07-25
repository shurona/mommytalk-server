package com.shrona.mommytalk.user.presentation.controller;

import static com.shrona.mommytalk.channel.common.exception.ChannelErrorCode.CHANNEL_NOT_FOUND;

import com.shrona.mommytalk.channel.application.ChannelService;
import com.shrona.mommytalk.channel.common.exception.ChannelException;
import com.shrona.mommytalk.channel.domain.Channel;
import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.common.dto.PageResponseDto;
import com.shrona.mommytalk.user.application.UserService;
import com.shrona.mommytalk.user.domain.User;
import com.shrona.mommytalk.user.infrastructure.repository.dao.UserListProjection;
import com.shrona.mommytalk.user.presentation.dtos.request.UpdatePreferredSendTimeRequestDto;
import com.shrona.mommytalk.user.presentation.dtos.request.UpdateUserRequestDto;
import com.shrona.mommytalk.user.presentation.dtos.response.PreferredSendTimeResponseDto;
import com.shrona.mommytalk.user.presentation.dtos.response.SentenceHistoryResponseDto;
import com.shrona.mommytalk.user.presentation.dtos.response.TokenUsageResponseDto;
import com.shrona.mommytalk.user.presentation.dtos.response.UserListResponseDto;
import com.shrona.mommytalk.user.presentation.dtos.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.validation.annotation.Validated;
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
    private final ChannelService channelService;

    /**
     * 유저 목록 조회
     */
    @GetMapping
    public ApiResponse<PageResponseDto<UserListResponseDto>> findUserList(
        @RequestParam(required = false, defaultValue = "10") Integer size,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "DATE") String sort,
        @RequestParam(defaultValue = "ASC") String direction,
        @RequestParam(value = "search", required = false) String searchToken,
        @PathVariable Long channelId
    ) {

        Sort sortInfo =
            direction.equalsIgnoreCase("DESC") ?
                Sort.by(Order.desc("createdAt"))
                : Sort.by(Order.asc("createdAt"));
        PageRequest pageRequest = PageRequest.of(page, size, sortInfo);

        Page<UserListProjection> userLists = userService.findUserListByChannelInfoWithPaging(
            channelId, pageRequest, searchToken);

        return ApiResponse.success(
            PageResponseDto.from(
                userLists.stream().map(UserListResponseDto::from).toList(),
                userLists.getNumber(),
                userLists.getSize(),
                userLists.getTotalElements(),
                userLists.getTotalPages())
        );
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponseDto> findUserById(
        @PathVariable Long channelId,
        @PathVariable Long userId
    ) {
        Channel channel = channelService.findChannelById(channelId)
            .orElseThrow(() -> new ChannelException(CHANNEL_NOT_FOUND));

        UserResponseDto userInfoById = userService.findUserInfoById(channel, userId);
        return ApiResponse.success(userInfoById);
    }

    @PutMapping("/{userId}")
    public ApiResponse<String> updateUserInfo(
        @PathVariable Long userId,
        @RequestBody UpdateUserRequestDto requestDto
    ) {

        // 유저 업데이트
        userService.updateUserInfoByAdmin(userId, requestDto);

        return ApiResponse.success("success");
    }

    /**
     * 어드민이 유저의 선호 발송 시간을 변경한다. (클라이언트와 동일하게 다음날부터 적용)
     */
    @PatchMapping("/{userId}/preferred-send-time")
    public ApiResponse<PreferredSendTimeResponseDto> updatePreferredSendTime(
        @PathVariable Long userId,
        @Validated @RequestBody UpdatePreferredSendTimeRequestDto requestBody
    ) {
        User user = userService.updatePreferredSendTime(userId, requestBody.preferredSendTime());

        return ApiResponse.success(PreferredSendTimeResponseDto.from(user));
    }

    /**
     * 채널별 유저 문장 이력 조회 (관리자용)
     * - 선택적 날짜 필터링 (year, month, day)
     * - 선택적 유저 필터링 (userId)
     * - 페이징 지원
     */
    @GetMapping("/sentence-history")
    public ApiResponse<PageResponseDto<SentenceHistoryResponseDto>> findSentenceHistory(
        @PathVariable Long channelId,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer day,
        @RequestParam(required = false) Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<SentenceHistoryResponseDto> result = userService.findSentenceHistoryByChannel(
            channelId, year, month, day, userId, pageRequest
        );

        return ApiResponse.success(
            PageResponseDto.from(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
            )
        );
    }

    /**
     * 채널별 토큰 사용량 합계 조회 (관리자용)
     * - 선택적 날짜 필터링 (year, month, day)
     * - 선택적 유저 필터링 (userId)
     */
    @GetMapping("/sentence-history/token-usage")
    public ApiResponse<TokenUsageResponseDto> getTokenUsageSum(
        @PathVariable Long channelId,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Integer day,
        @RequestParam(required = false) Long userId
    ) {
        TokenUsageResponseDto result = userService.getTokenUsageSumByChannel(
            channelId, year, month, day, userId
        );

        return ApiResponse.success(result);
    }
}
