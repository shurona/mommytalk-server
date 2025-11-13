package com.shrona.mommytalk.entitlement.presentation.controller;

import com.shrona.mommytalk.common.dto.ApiResponse;
import com.shrona.mommytalk.entitlement.application.UserEntitlementService;
import com.shrona.mommytalk.entitlement.domain.UserEntitlement;
import com.shrona.mommytalk.entitlement.presentation.dtos.request.AddUserEntitlementRequestDto;
import com.shrona.mommytalk.entitlement.presentation.dtos.request.UpdateUserEntitlementRequestDto;
import com.shrona.mommytalk.entitlement.presentation.dtos.response.UserEntitlementResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/channels/{channelId}/user-entitlements")
@RestController
public class UserEntitlementRestController {

    private final UserEntitlementService userEntitlementService;

    /**
     * 유저 상품권 추가 (AUTO_ACTIVE 그룹 자동 할당)
     */
    @PostMapping
    public ApiResponse<Long> addUserEntitlement(
        @PathVariable Long channelId,
        @Valid @RequestBody AddUserEntitlementRequestDto requestDto
    ) {
        log.info("[유저 상품권 추가] channelId={}, userId={}, entitlementId={}",
            channelId, requestDto.userId(), requestDto.entitlementId());

        UserEntitlement userEntitlement = userEntitlementService.addUserEntitlement(requestDto);

        return ApiResponse.success(userEntitlement.getId());
    }

    /**
     * 유저 상품권 수정 (상태 변경 및 종료일 연장)
     */
    @PatchMapping("/{userEntitlementId}")
    public ApiResponse<String> updateUserEntitlement(
        @PathVariable Long channelId,
        @PathVariable Long userEntitlementId,
        @Valid @RequestBody UpdateUserEntitlementRequestDto requestDto
    ) {
        log.info("[유저 상품권 수정] channelId={}, userEntitlementId={}, status={}, endDate={}",
            channelId, userEntitlementId, requestDto.status(), requestDto.endDate());

        userEntitlementService.updateUserEntitlement(userEntitlementId, requestDto);

        return ApiResponse.success("success");
    }

    /**
     * 유저의 상품권 목록 조회
     */
    @GetMapping("/users/{userId}")
    public ApiResponse<List<UserEntitlementResponseDto>> getUserEntitlements(
        @PathVariable Long channelId,
        @PathVariable Long userId
    ) {
        log.info("[유저 상품권 조회] channelId={}, userId={}", channelId, userId);

        List<UserEntitlementResponseDto> entitlements =
            userEntitlementService.getUserEntitlements(channelId, userId);

        return ApiResponse.success(entitlements);
    }
}
